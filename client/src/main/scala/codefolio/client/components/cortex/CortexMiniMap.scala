package codefolio.client.components.cortex

import codefolio.client.util.HashScroll
import codefolio.client.util.ReaderState
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js

/**
 * Right-edge vertical mini-map: a thin track with one tick per H3 in the chapter prose, a fill that rises
 * with scroll, and labels that surface on tile hover. Click a tick to jump.
 *
 *   - Ticks are placed proportionally to each H3's vertical position inside `.chapter-content` at mount, and
 *     rebuilt on resize and when the type-controls dispatch `cortex:typePrefsChanged` (prose height changes
 *     with font size).
 *   - Fill height tracks `ReaderState.currentScrollFraction()` on every scroll.
 *   - Active tick mirrors the right-TOC's active heading via the same `cortex:activeHeading` event the sticky
 *     bar listens to.
 *   - Hidden under 1100px (matches the preview) and in focus mode (CSS rules in `cortex-reader.css`).
 */
object CortexMiniMap:

  private case class Tick(slug: String, text: String, topPct: Double)

  /**
   * Wait for ChapterContent to mount its dangerouslySetInnerHTML payload before measuring headings. 250ms is
   * comfortably longer than React's commit + the next browser paint, but well below the point where a user
   * notices the mini-map appearing.
   */
  private val BuildDelayMs = 250

  /**
   * Recompute tick positions. Returns an empty list if the prose container is missing or too short to
   * meaningfully map.
   */
  private def measureTicks(): List[Tick] =
    val prose = dom.document.querySelector(".chapter-content")
    if prose == null then List.empty
    else
      val rect        = prose.asInstanceOf[dom.html.Element].getBoundingClientRect()
      val proseHeight = rect.height
      if proseHeight < 200 then List.empty
      else
        val proseTopAbs = rect.top + dom.window.scrollY
        val nodes       = prose.querySelectorAll(".chapter-content h3[id]")
        val len         = nodes.length
        val buf         = scala.collection.mutable.ListBuffer.empty[Tick]
        var i           = 0
        while i < len do
          val h      = nodes.item(i).asInstanceOf[dom.html.Element]
          val topAbs = h.getBoundingClientRect().top + dom.window.scrollY
          val pct    = ((topAbs - proseTopAbs) / proseHeight).max(0.0).min(1.0)
          val id     = h.id
          val text   = Option(h.textContent).getOrElse("").trim
          if id.nonEmpty && text.nonEmpty then buf += Tick(id, text, pct)
          i += 1
        buf.toList

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useState(List.empty[Tick])
      .useState(0.0)
      .useState(Option.empty[String])
      // Build ticks shortly after mount, on resize, and whenever type prefs change.
      .useEffectOnMountBy { (_, ticksS, _, _) =>
        Callback {
          def rebuild(): Unit =
            ticksS.setState(measureTicks()).runNow()

          dom.window.setTimeout(() => rebuild(), BuildDelayMs.toDouble)
          val onResize: js.Function1[dom.Event, Unit] = (_: dom.Event) => rebuild()
          val onPrefs: js.Function1[dom.Event, Unit] = (_: dom.Event) =>
            // Defer one frame so layout has reflowed under the new --reader-fs / --reader-lh / etc.
            val _ = dom.window.setTimeout(() => rebuild(), 0)
          dom.window.addEventListener("resize", onResize, useCapture = false)
          dom.window.addEventListener("cortex:typePrefsChanged", onPrefs, useCapture = false)
          ()
        }
      }
      // Scroll listener — drive the fill height.
      .useEffectOnMountBy { (_, _, fillS, _) =>
        Callback {
          val onScroll: js.Function1[dom.Event, Unit] = (_: dom.Event) =>
            fillS.setState(ReaderState.currentScrollFraction()).runNow()
          dom.window.addEventListener("scroll", onScroll, useCapture = false)
          fillS.setState(ReaderState.currentScrollFraction()).runNow()
          ()
        }
      }
      // Active-tick — listen for the same active-heading event the sticky bar uses.
      .useEffectOnMountBy { (_, _, _, activeS) =>
        Callback {
          val onActive: js.Function1[dom.CustomEvent, Unit] = (e: dom.CustomEvent) =>
            val slug = e.detail.asInstanceOf[String]
            activeS.setState(Option(slug).filter(_.nonEmpty)).runNow()
          dom.window.addEventListener(
            "cortex:activeHeading",
            onActive.asInstanceOf[js.Function1[dom.Event, Unit]],
            useCapture = false
          )
          ()
        }
      }
      .render { (_, ticksS, fillS, activeS) =>
        val ticks = ticksS.value
        if ticks.isEmpty then EmptyVdom
        else
          val active = activeS.value
          val fillStyle =
            js.Dynamic.literal(height = s"${(fillS.value * 100).max(0.0).min(100.0)}%").asInstanceOf[js.Object]
          <.aside(
            ^.className  := "cortex-reader-minimap",
            ^.aria.label := "Article mini-map",
            <.div(
              ^.className := "cortex-reader-minimap__track",
              <.div(^.className := "cortex-reader-minimap__fill", ^.style := fillStyle),
              ticks.toTagMod { tick =>
                val isActive = active.contains(tick.slug)
                val cls =
                  if isActive then "cortex-reader-minimap__tick cortex-reader-minimap__tick--active"
                  else "cortex-reader-minimap__tick"
                <.button(
                  ^.key        := s"mm-${tick.slug}",
                  ^.tpe        := "button",
                  ^.className  := cls,
                  ^.aria.label := s"Jump to ${tick.text}",
                  ^.style := js.Dynamic.literal(top = s"${(tick.topPct * 100).max(0.0).min(100.0)}%")
                    .asInstanceOf[js.Object],
                  ^.onClick --> Callback(HashScroll.scrollTo(tick.slug)),
                  <.span(^.className := "cortex-reader-minimap__tick-label", tick.text)
                )
              }
            )
          )
      }
