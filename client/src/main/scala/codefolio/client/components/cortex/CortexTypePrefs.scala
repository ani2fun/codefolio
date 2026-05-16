package codefolio.client.components.cortex

import codefolio.client.components.icons.LucideIcons
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

import scala.scalajs.js
import scala.util.Try

/**
 * Reading preferences panel + FAB. Four controls, all persisted to localStorage and applied as CSS
 * custom properties on `document.documentElement`:
 *
 *   - **Size** — `s` / `m` / `l` → `--reader-fs: 16 / 18.5 / 21 px` on the prose root.
 *   - **Leading** — `tight` / `comfortable` / `loose` → `--reader-lh: 1.55 / 1.8 / 2.05`.
 *   - **Family** — `sans` / `serif` → swaps `--reader-font` between the site sans and an Iowan/Charter
 *     serif body stack (Instrument Serif italic is too heavy for body text — used only for display).
 *   - **Dark mode** — toggles `html.dark`, which the existing Tailwind v4 token palette already covers.
 *
 * Triggered by the Aa FAB in the right-edge fab stack OR the `T` key (skipped inside editable targets).
 * Panel is dismissed by `Esc`, clicks outside, or pressing `T`/Aa again.
 *
 * Dispatches `cortex:typePrefsChanged` after every apply so the mini-map can rebuild its tick positions
 * (prose height shifts with font size).
 */
object CortexTypePrefs:

  private val StorageKey = "cortex-reader.typePrefs"

  private val SizeMap = Map("s" -> "16px", "m" -> "18.5px", "l" -> "21px")
  private val LeadMap = Map("tight" -> "1.55", "comfortable" -> "1.8", "loose" -> "2.05")
  private val FontMap = Map(
    "sans"  -> "var(--font-sans)",
    "serif" -> "\"Iowan Old Style\", \"Charter\", Georgia, serif"
  )

  private case class Prefs(size: String, lead: String, font: String, dark: Boolean):
    def withSize(s: String): Prefs = copy(size = s)
    def withLead(l: String): Prefs = copy(lead = l)
    def withFont(f: String): Prefs = copy(font = f)
    def toggleDark: Prefs          = copy(dark = !dark)

  private val Defaults = Prefs("m", "comfortable", "sans", dark = false)

  private def readPrefs(): Prefs =
    Try {
      val raw = dom.window.localStorage.getItem(StorageKey)
      if raw == null then Defaults
      else
        val obj = js.JSON.parse(raw).asInstanceOf[js.Dynamic]
        Prefs(
          size = Option(obj.size.asInstanceOf[String]).getOrElse(Defaults.size),
          lead = Option(obj.lead.asInstanceOf[String]).getOrElse(Defaults.lead),
          font = Option(obj.font.asInstanceOf[String]).getOrElse(Defaults.font),
          dark = Option(obj.dark.asInstanceOf[Boolean]).getOrElse(Defaults.dark)
        )
    }.getOrElse(Defaults)

  private def writePrefs(p: Prefs): Unit =
    val obj = js.Dynamic.literal(size = p.size, lead = p.lead, font = p.font, dark = p.dark)
    Try(dom.window.localStorage.setItem(StorageKey, js.JSON.stringify(obj))).getOrElse(())

  /** Apply prefs by writing CSS custom properties to the documentElement and toggling the dark class.
    * Dispatches a `cortex:typePrefsChanged` event so dependent widgets (mini-map) can re-measure. */
  private def applyPrefs(p: Prefs): Unit =
    val style = dom.document.documentElement.asInstanceOf[js.Dynamic].style
    style.setProperty("--reader-fs", SizeMap.getOrElse(p.size, "18.5px"))
    style.setProperty("--reader-lh", LeadMap.getOrElse(p.lead, "1.8"))
    style.setProperty("--reader-font", FontMap.getOrElse(p.font, "var(--font-sans)"))
    val classList = dom.document.documentElement.classList
    if p.dark then classList.add("dark") else classList.remove("dark")
    val init = (new js.Object).asInstanceOf[dom.CustomEventInit]
    init.detail = p.size
    val ev = new dom.CustomEvent("cortex:typePrefsChanged", init)
    val _ = dom.window.dispatchEvent(ev)

  private def isEditableTarget(e: dom.KeyboardEvent): Boolean =
    val t = e.target.asInstanceOf[dom.Element]
    t != null && (t.matches("input, textarea, [contenteditable], [contenteditable='true']"))

  val Component =
    ScalaFnComponent
      .withHooks[Unit]
      .useState(false)
      .useState(Defaults)
      .useEffectOnMountBy { (_, _, prefsS) =>
        Callback {
          val initial = readPrefs()
          prefsS.setState(initial).runNow()
          applyPrefs(initial)
        }
      }
      .useEffectOnMountBy { (_, openS, _) =>
        Callback {
          val onKey: js.Function1[dom.KeyboardEvent, Unit] = (e: dom.KeyboardEvent) =>
            if !isEditableTarget(e) then
              if (e.key == "t" || e.key == "T") && !e.metaKey && !e.ctrlKey && !e.altKey then
                e.preventDefault()
                openS.setState(!openS.value).runNow()
              else if e.key == "Escape" && openS.value then openS.setState(false).runNow()
          dom.window.addEventListener("keydown", onKey, useCapture = false)
          ()
        }
      }
      // Click outside dismisses the panel. The FAB stops propagation so clicking it stays as the
      // toggle path rather than dismissing.
      .useEffectOnMountBy { (_, openS, _) =>
        Callback {
          val onDown: js.Function1[dom.MouseEvent, Unit] = (e: dom.MouseEvent) =>
            if openS.value then
              val t = e.target.asInstanceOf[dom.Element]
              val panel = dom.document.querySelector(".cortex-reader-type-prefs")
              val fab   = dom.document.querySelector(".cortex-reader-type-prefs-fab")
              val inside =
                (panel != null && t != null && panel.contains(t)) ||
                  (fab != null && t != null && fab.contains(t))
              if !inside then openS.setState(false).runNow()
          dom.document.addEventListener("mousedown", onDown, useCapture = false)
          ()
        }
      }
      .render { (_, openS, prefsS) =>
        val p = prefsS.value

        def setAndPersist(next: Prefs): Callback = Callback {
          prefsS.setState(next).runNow()
          writePrefs(next)
          applyPrefs(next)
        }

        def seg(group: String, current: String, value: String, label: String): VdomNode =
          <.button(
            ^.key                  := s"$group-$value",
            ^.tpe                  := "button",
            VdomAttr("data-tc")    := group,
            VdomAttr("data-v")     := value,
            ^.aria.pressed         := (current == value),
            ^.onClick --> setAndPersist(group match
              case "size" => p.withSize(value)
              case "lead" => p.withLead(value)
              case "font" => p.withFont(value)
              case _      => p
            ),
            label
          )

        <.div(
          // FAB
          <.button(
            ^.tpe         := "button",
            ^.className   := "cortex-reader-type-prefs-fab",
            ^.aria.label  := "Reading preferences (T)",
            ^.aria.expanded := openS.value,
            ^.aria.controls := "cortex-reader-type-prefs",
            ^.onClick --> openS.setState(!openS.value),
            LucideIcons.Type(LucideIcons.withClass("cortex-reader-type-prefs-fab__icon"))
          ),
          // Panel
          <.aside(
            ^.id                  := "cortex-reader-type-prefs",
            ^.className           := "cortex-reader-type-prefs",
            VdomAttr("data-open") := (if openS.value then "true" else "false"),
            ^.role                := "dialog",
            ^.aria.label          := "Reading preferences",
            <.h3(^.className := "cortex-reader-type-prefs__title", "Reading preferences"),
            // Size
            <.div(
              ^.className := "cortex-reader-type-prefs__group",
              <.div(^.className := "cortex-reader-type-prefs__label", <.span("Size")),
              <.div(
                ^.className := "cortex-reader-type-prefs__seg",
                ^.role      := "radiogroup",
                ^.aria.label := "Font size",
                seg("size", p.size, "s", "Small"),
                seg("size", p.size, "m", "Medium"),
                seg("size", p.size, "l", "Large")
              )
            ),
            // Leading
            <.div(
              ^.className := "cortex-reader-type-prefs__group",
              <.div(^.className := "cortex-reader-type-prefs__label", <.span("Leading")),
              <.div(
                ^.className := "cortex-reader-type-prefs__seg",
                ^.role      := "radiogroup",
                ^.aria.label := "Line height",
                seg("lead", p.lead, "tight", "Tight"),
                seg("lead", p.lead, "comfortable", "Comfortable"),
                seg("lead", p.lead, "loose", "Loose")
              )
            ),
            // Family
            <.div(
              ^.className := "cortex-reader-type-prefs__group",
              <.div(^.className := "cortex-reader-type-prefs__label", <.span("Type family")),
              <.div(
                ^.className := "cortex-reader-type-prefs__seg cortex-reader-type-prefs__seg--family",
                ^.role      := "radiogroup",
                ^.aria.label := "Type family",
                seg("font", p.font, "sans", "Sans"),
                seg("font", p.font, "serif", "Serif")
              )
            ),
            // Dark mode toggle
            <.div(
              ^.className := "cortex-reader-type-prefs__group",
              <.button(
                ^.tpe          := "button",
                ^.className    := "cortex-reader-type-prefs__row",
                ^.aria.pressed := p.dark,
                ^.onClick --> setAndPersist(p.toggleDark),
                <.span(^.className := "cortex-reader-type-prefs__row-label", "Dark mode"),
                <.span(^.className := "cortex-reader-type-prefs__switch", ^.aria.hidden := true)
              )
            )
          )
        )
      }
