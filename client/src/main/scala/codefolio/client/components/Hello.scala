package codefolio.client.components

import codefolio.client.api.ApiClient
import codefolio.shared.api.Endpoints.{Greeting, HelloEvent, RecentCalls}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

object Hello:

  final private case class State(
      greeting: Option[Either[String, Greeting]],
      recent: Option[Either[String, RecentCalls]]
  )

  private object State:
    val empty: State = State(None, None)

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useState(State.empty)
      .useEffectOnMountBy { (_, state) =>
        // Fetch greeting + recent log in parallel.
        val helloC = Callback.future {
          ApiClient.getHello.transform {
            case Success(g) => Success(state.modState(_.copy(greeting = Some(Right(g)))))
            case Failure(e) => Success(state.modState(_.copy(greeting = Some(Left(e.getMessage)))))
          }
        }
        val recentC = Callback.future {
          ApiClient.getRecent.transform {
            case Success(r) => Success(state.modState(_.copy(recent = Some(Right(r)))))
            case Failure(e) => Success(state.modState(_.copy(recent = Some(Left(e.getMessage)))))
          }
        }
        helloC >> recentC
      }
      .render { (_, state) =>
        <.div(
          ^.className := "min-h-screen bg-slate-50 flex items-center justify-center px-4 py-8",
          <.div(
            ^.className := "bg-white shadow-xl rounded-2xl p-8 max-w-xl w-full space-y-6",
            <.h1(^.className := "text-3xl font-bold text-slate-900", "Codefolio"),
            renderGreeting(state.value.greeting),
            renderRecent(state.value.recent)
          )
        )
      }

  private def renderGreeting(g: Option[Either[String, Greeting]]): VdomNode =
    <.section(
      ^.className := "border-t border-slate-100 pt-4",
      <.h2(^.className := "text-sm font-semibold uppercase tracking-wide text-slate-500 mb-2", "Greeting"),
      g match
        case None            => <.p(^.className := "text-slate-500", "Loading…")
        case Some(Left(err)) => <.p(^.className := "text-red-600", s"Error: $err")
        case Some(Right(gr)) =>
          <.div(
            <.p(^.className := "text-lg text-slate-800", gr.message),
            <.p(^.className := "text-sm text-slate-500 mt-1", s"Visit count: ${gr.visits}"),
            <.p(
              ^.className := (if gr.cached then "text-xs text-amber-600 mt-1"
                              else "text-xs text-emerald-600 mt-1"),
              if gr.cached then "↺ served from Redis cache"
              else "✓ fresh read from Postgres"
            )
          )
    )

  private def renderRecent(r: Option[Either[String, RecentCalls]]): VdomNode =
    <.section(
      ^.className := "border-t border-slate-100 pt-4",
      <.h2(
        ^.className := "text-sm font-semibold uppercase tracking-wide text-slate-500 mb-2",
        "Recent calls (MongoDB)"
      ),
      r match
        case None            => <.p(^.className := "text-slate-500", "Loading…")
        case Some(Left(err)) => <.p(^.className := "text-red-600", s"Error: $err")
        case Some(Right(rc)) =>
          if rc.entries.isEmpty then
            <.p(^.className := "text-slate-400 italic", "No entries yet — refresh after the first call.")
          else
            <.ul(
              ^.className := "text-sm text-slate-700 space-y-1",
              rc.entries.toTagMod { (e: HelloEvent) =>
                <.li(
                  ^.key := s"${e.timestampEpochMs}-${e.visits}",
                  <.span(^.className := "text-slate-400 mr-2", formatTime(e.timestampEpochMs)),
                  <.span(s"visits=${e.visits}")
                )
              }
            )
    )

  private def formatTime(epochMs: Long): String =
    new js.Date(epochMs.toDouble).toISOString()
