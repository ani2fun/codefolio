package codefolio.client.components.sections

import codefolio.client.Page
import codefolio.client.components.ToggleMode
import codefolio.client.components.icons.LucideIcons
import japgolly.scalajs.react.*
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

/**
 * Sticky top nav with desktop pill menu and mobile drawer.
 *
 * Every entry is an in-page `#section` anchor (Work / About / Experience / Projects). Clicking one scrolls to
 * the matching `id` if it's in the DOM, otherwise it navigates to the home URL with the fragment so
 * [[codefolio.client.pages.HomePage]]'s mount effect performs the scroll.
 */
object Header:

  final case class Props(ctl: RouterCtl[Page])

  final private case class HashLink(hash: String, label: String)

  private val menuLinks: List[HashLink] = List(
    HashLink("/#work", "Work"),
    HashLink("/#about", "About"),
    HashLink("/#experience", "Experience"),
    HashLink("/#projects", "Projects")
  )

  /** Sister-site link — the Cortex book lives at its own domain (the other half of the split). */
  private val CortexUrl = "https://cortex.kakde.eu"

  /**
   * Scroll to the section if its id is in the DOM, otherwise navigate to the home URL with the fragment so
   * HomePage's mount effect handles the scroll. `replaceState` keeps the fragment in the URL for
   * shareability; `location.assign` is the bulletproof fallback for cross-route nav.
   */
  private def goToAnchor(hash: String): Callback =
    Callback {
      val id     = hash.stripPrefix("/#").stripPrefix("#")
      val target = dom.document.getElementById(id)
      if target != null then
        target.scrollIntoView(true)
        dom.window.history.replaceState(null, "", "#" + id)
      else dom.window.location.assign(hash)
    }

  val Component =
    ScalaFnComponent
      .withHooks[Props]
      .useState(false)
      .render { (_, menuOpenS) =>
        val toggleMenu = menuOpenS.modState(!_)

        def linkClick(hash: String): ReactEventFromInput => Callback =
          (e: ReactEventFromInput) => e.preventDefaultCB >> goToAnchor(hash)

        def desktopLink(link: HashLink): VdomNode =
          <.a(
            ^.key       := link.hash,
            ^.href      := link.hash,
            ^.className := "header__link",
            ^.onClick ==> linkClick(link.hash),
            link.label
          )

        def mobileLink(link: HashLink): VdomNode =
          <.li(
            ^.key := link.hash,
            <.a(
              ^.href      := link.hash,
              ^.className := "header__drawer-link",
              ^.onClick ==> ((e: ReactEventFromInput) =>
                e.preventDefaultCB >> menuOpenS.setState(false) >> goToAnchor(link.hash)
              ),
              link.label
            )
          )

        <.header(
          ^.className := "header",
          <.nav(
            ^.className := "header__nav container",
            <.a(
              ^.href      := "/#hero",
              ^.className := "header__brand",
              ^.onClick ==> linkClick("/#hero"),
              <.span(^.className := "header__logomark", "a"),
              <.span(^.className := "header__wordmark", "aniket.kakde")
            ),
            <.div(
              ^.className := "header__menu",
              menuLinks.toTagMod(desktopLink),
              <.a(
                ^.key       := "cortex",
                ^.href      := CortexUrl,
                ^.className := "header__link",
                "Cortex",
                LucideIcons.ExternalLink(LucideIcons.withClass("header__link-icon"))
              )
            ),
            <.div(
              ^.className := "header__actions",
              <.div(^.className := "header__toggle--mobile", ToggleMode.Component()),
              <.div(^.className := "header__toggle--desktop", ToggleMode.Component()),
              <.a(
                ^.href      := "mailto:a.r.kakde@gmail.com",
                ^.className := "header__cta",
                "Get in touch"
              ),
              <.button(
                ^.className  := "header__burger",
                ^.aria.label := (if menuOpenS.value then "Close menu" else "Open menu"),
                ^.onClick --> toggleMenu,
                if menuOpenS.value then
                  LucideIcons.X(LucideIcons.withClass("header__burger-icon"))
                else LucideIcons.Menu(LucideIcons.withClass("header__burger-icon"))
              )
            )
          ),
          if menuOpenS.value then
            <.nav(
              ^.className := "header__drawer",
              <.ul(
                ^.className := "header__drawer-list",
                menuLinks.toTagMod(mobileLink),
                <.li(
                  ^.key := "cortex",
                  <.a(
                    ^.href      := CortexUrl,
                    ^.className := "header__drawer-link",
                    "Cortex",
                    LucideIcons.ExternalLink(LucideIcons.withClass("header__link-icon"))
                  )
                )
              )
            )
          else EmptyVdom
        )
      }
