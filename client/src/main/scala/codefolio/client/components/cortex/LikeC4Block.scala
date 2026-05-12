package codefolio.client.components.cortex

import codefolio.client.components.icons.LucideIcons
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js

/**
 * Wraps a LikeC4 view iframe (served from the `/c4` proxy) with a hover-visible Zoom button that opens a
 * near-fullscreen modal. The LikeC4 SPA owns pan/zoom inside the diagram itself, so unlike [[D2Diagram]] this
 * component does not apply an outer CSS scale — it just gives the SPA a bigger viewport to render into.
 *
 * The inline iframe and the modal iframe are two distinct `<iframe>` elements with the same `src`. Re-
 * parenting the original iframe into the modal would reload the LikeC4 SPA inside it and lose any pan / zoom
 * state the reader had set up.
 */
object LikeC4Block:

  private val DefaultHeight = 520

  // `loading` is a relatively new HTML attribute; scalajs-react's typed `^.` namespace
  // doesn't expose it yet, so build it via the generic `VdomAttr` escape hatch.
  private val loadingAttr = VdomAttr[String]("loading")

  final case class Props(src: String, height: Option[Int], title: Option[String])

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(false)
      .useRefBy(_ => js.Array[js.Function0[Unit]]())
      .useEffectWithDepsBy((_, openS, _) => openS.value) { (_, openS, cleanupRef) => isOpen =>
        val tearDown: Callback = Callback {
          val arr = cleanupRef.value
          for i <- 0 until arr.length do arr(i)()
          cleanupRef.value = js.Array()
        }

        val install: Callback =
          if !isOpen then Callback.empty
          else
            Callback {
              val previousOverflow = dom.document.body.style.overflow
              dom.document.body.style.overflow = "hidden"

              val onKey: js.Function1[dom.KeyboardEvent, Unit] = (e: dom.KeyboardEvent) =>
                if e.key == "Escape" then
                  e.preventDefault()
                  openS.setState(false).runNow()

              dom.document.addEventListener("keydown", onKey)
              cleanupRef.value.push { () =>
                dom.document.removeEventListener("keydown", onKey)
                dom.document.body.style.overflow = previousOverflow
                ()
              }
              ()
            }

        tearDown >> install
      }
      .render { (props, openS, _) =>
        val open: Callback  = openS.setState(true)
        val close: Callback = openS.setState(false)

        val labelTitle = props.title.getOrElse("LikeC4 diagram")

        <.div(
          <.div(
            ^.className := "likec4-iframe group not-prose",
            <.iframe(
              ^.className := "likec4-iframe__frame",
              ^.src       := props.src,
              ^.title     := labelTitle,
              // scalajs-react's typed `^.height` is silently dropped on
              // <iframe> in the React VDOM layer (verified in the DOM —
              // the attribute never lands). Use inline CSS instead.
              ^.style := js.Dynamic
                .literal(height = s"${props.height.getOrElse(DefaultHeight)}px")
                .asInstanceOf[js.Object],
              loadingAttr := "lazy"
            ),
            <.button(
              ^.tpe := "button",
              ^.onClick --> open,
              ^.aria.label := "Open diagram in fullscreen",
              ^.className  := "likec4-iframe__zoom-button",
              LucideIcons.Maximize2(LucideIcons.withClass("likec4-iframe__zoom-icon")),
              "Zoom"
            )
          ),
          if openS.value then
            <.div(
              ^.role       := "dialog",
              ^.aria.modal := true,
              ^.aria.label := s"$labelTitle — fullscreen view",
              ^.className  := "likec4-modal not-prose",
              ^.onClick --> close,
              <.div(
                ^.className := "likec4-modal__card",
                ^.onClick ==> { (e: ReactEvent) => e.stopPropagationCB },
                <.iframe(
                  ^.className := "likec4-modal__frame",
                  ^.src       := props.src,
                  ^.title     := labelTitle,
                  loadingAttr := "lazy"
                ),
                // LikeC4's standalone viewer hides the React Flow controls panel
                // (`enableControls: false` is hardcoded in the bundle), so we
                // surface the mouse-wheel + drag affordance ourselves. Synthetic
                // +/- buttons would be fragile — d3-zoom under React Flow rejects
                // non-trusted wheel events, so dispatchEvent doesn't move the
                // viewport. The hint is the honest UX cue.
                <.div(
                  ^.className   := "likec4-modal__hint",
                  ^.aria.hidden := true,
                  "Scroll to zoom · Drag to pan"
                ),
                <.button(
                  ^.tpe := "button",
                  ^.onClick --> close,
                  ^.aria.label := "Close",
                  ^.className  := "likec4-modal__close",
                  LucideIcons.X(LucideIcons.withClass("likec4-modal__close-icon"))
                )
              )
            )
          else EmptyVdom
        )
      }
