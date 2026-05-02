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
 * Hash-link anchors (`/#about`, `/#experience`, …) scroll to the matching `id` if the section is already in
 * the DOM. If we're on a non-home route (e.g. `/knowledge/...`), we fall back to a real URL navigation —
 * [[codefolio.client.pages.HomePage]]'s `useEffectOnMountBy` reads the URL fragment after mount and scrolls.
 */
object Header:

  final case class Props(ctl: RouterCtl[Page])

  /** Menu items shown in both the desktop bar and the mobile drawer. */
  private val menuLinks: List[(String, String)] = List(
    "/#about"          -> "About",
    "/#experience"     -> "Experience",
    "/#projects"       -> "Projects",
    "/#certifications" -> "Certifications",
    "/#knowledge"      -> "Knowledge Base"
  )

  /**
   * Scroll to the section if its id is in the DOM, otherwise navigate to the home URL with the fragment so
   * HomePage's mount effect handles the scroll.
   *
   * We DON'T use `RouterCtl.set(Page.Home)` here: it computes the canonical URL for `Page.Home` (`/`) and
   * pushes that, wiping the fragment we want. `replaceState` keeps the fragment in the URL for shareability,
   * and `location.assign` is the bulletproof fallback for cross-route nav.
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
      .useState(false) // mobile menuOpen
      .render { (props, menuOpenS) =>
        val toggleMenu = menuOpenS.modState(!_)

        def linkClick(hash: String): ReactEventFromInput => Callback =
          (e: ReactEventFromInput) => e.preventDefaultCB >> goToAnchor(hash)

        def mobileLink(hash: String, label: String): VdomNode =
          <.li(
            ^.key := hash,
            <.a(
              ^.href      := hash,
              ^.className := "font-semibold hover:text-primary dark:hover:text-primary",
              ^.onClick ==> ((e: ReactEventFromInput) =>
                e.preventDefaultCB >> menuOpenS.setState(false) >> goToAnchor(hash)
              ),
              label
            )
          )

        def desktopLink(hash: String, label: String): VdomNode =
          <.a(
            ^.key  := hash,
            ^.href := hash,
            ^.className :=
              "px-2 lg:px-3 py-2 hover:bg-primary dark:hover:bg-primary hover:text-gray-50 " +
                "text-base lg:text-xl font-semibold rounded",
            ^.onClick ==> linkClick(hash),
            label
          )

        <.header(
          ^.className := "fixed top-0 left-0 right-0 z-50 bg-white dark:bg-background shadow-md opacity-95",
          <.nav(
            ^.className := "container mx-auto p-5 flex justify-between items-center",
            // Site title (desktop)
            <.a(
              ^.href := "/#hero",
              ^.className :=
                "hidden md:block text-lg md:text-2xl lg:text-3xl font-bold text-primary dark:text-primary",
              ^.onClick ==> linkClick("/#hero"),
              "Aniket Kakde"
            ),
            // Mobile theme toggle
            <.div(^.className := "block md:hidden", ToggleMode.Component()),
            // Desktop menu links
            <.div(
              ^.className := "hidden md:flex md:space-x-2 lg:space-x-5",
              menuLinks.toTagMod((hash, label) => desktopLink(hash, label))
            ),
            // Desktop theme toggle
            <.div(^.className := "hidden md:flex", ToggleMode.Component()),
            // Mobile burger button
            <.button(
              ^.className  := "md:hidden",
              ^.aria.label := (if menuOpenS.value then "Close menu" else "Open menu"),
              ^.onClick --> toggleMenu,
              if menuOpenS.value then
                LucideIcons.X(
                  LucideIcons.withClass(
                    "w-10 h-10 text-primary dark:text-primary hover:text-rose-500"
                  )
                )
              else
                LucideIcons.Menu(
                  LucideIcons.withClass(
                    "w-10 h-10 text-primary dark:text-primary hover:text-rose-500"
                  )
                )
            )
          ),
          // Mobile drawer
          if menuOpenS.value then
            <.nav(
              ^.className := "md:hidden",
              <.ul(
                ^.className := "flex flex-col items-left space-y-5 bg-white dark:bg-background p-5",
                menuLinks.toTagMod((hash, label) => mobileLink(hash, label))
              )
            )
          else EmptyVdom
        )
      }
