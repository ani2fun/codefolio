package codefolio.client.components.icons

import japgolly.scalajs.react.*

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/** Typed bindings for the small subset of lucide-react icons we use.
  *
  * Each icon is a React function component that accepts standard SVG props
  * plus the lucide-specific extras. We expose them through scalajs-react's
  * `JsComponent` machinery so they can be composed with VDOM like any other
  * React component — no `js.Dynamic` or hand-rolled wrappers.
  *
  * To add an icon: add a `@JSImport` for the named export and a `val` line
  * binding the raw object to a `JsComponent[IconProps, Children.None, Null]`.
  */
object LucideIcons:

  trait IconProps extends js.Object:
    var className: js.UndefOr[String]      = js.undefined
    var size: js.UndefOr[Int]              = js.undefined
    var strokeWidth: js.UndefOr[Double]    = js.undefined
    var color: js.UndefOr[String]          = js.undefined
    var absoluteStrokeWidth: js.UndefOr[Boolean] = js.undefined

  /** Build IconProps with just a className. Most call sites only need this. */
  def withClass(c: String): IconProps =
    val p = (new js.Object).asInstanceOf[IconProps]
    p.className = c
    p

  // ---- Raw imports (named exports of `lucide-react`) ----------------------

  @js.native @JSImport("lucide-react", "Sun")        private object SunRaw        extends js.Object
  @js.native @JSImport("lucide-react", "Moon")       private object MoonRaw       extends js.Object
  @js.native @JSImport("lucide-react", "Menu")       private object MenuRaw       extends js.Object
  @js.native @JSImport("lucide-react", "X")          private object XRaw          extends js.Object
  @js.native @JSImport("lucide-react", "ArrowRight") private object ArrowRightRaw extends js.Object
  @js.native @JSImport("lucide-react", "BookOpen")   private object BookOpenRaw   extends js.Object
  @js.native @JSImport("lucide-react", "Loader2")    private object Loader2Raw    extends js.Object
  @js.native @JSImport("lucide-react", "Play")       private object PlayRaw       extends js.Object
  @js.native @JSImport("lucide-react", "RotateCcw")  private object RotateCcwRaw  extends js.Object
  @js.native @JSImport("lucide-react", "Square")     private object SquareRaw     extends js.Object
  @js.native @JSImport("lucide-react", "Maximize2")    private object Maximize2Raw    extends js.Object
  @js.native @JSImport("lucide-react", "ZoomIn")       private object ZoomInRaw       extends js.Object
  @js.native @JSImport("lucide-react", "ZoomOut")      private object ZoomOutRaw      extends js.Object
  @js.native @JSImport("lucide-react", "Check")        private object CheckRaw        extends js.Object
  @js.native @JSImport("lucide-react", "Copy")         private object CopyRaw         extends js.Object
  @js.native @JSImport("lucide-react", "ChevronRight") private object ChevronRightRaw extends js.Object
  @js.native @JSImport("lucide-react", "ChevronDown")  private object ChevronDownRaw  extends js.Object
  @js.native @JSImport("lucide-react", "ListTree")     private object ListTreeRaw     extends js.Object
  @js.native @JSImport("lucide-react", "ArrowLeft")    private object ArrowLeftRaw    extends js.Object

  // ---- Components (call as Sun(withClass("h-5 w-5"))) --------------------

  val Sun        = JsComponent[IconProps, Children.None, Null](SunRaw)
  val Moon       = JsComponent[IconProps, Children.None, Null](MoonRaw)
  val Menu       = JsComponent[IconProps, Children.None, Null](MenuRaw)
  val X          = JsComponent[IconProps, Children.None, Null](XRaw)
  val ArrowRight = JsComponent[IconProps, Children.None, Null](ArrowRightRaw)
  val BookOpen   = JsComponent[IconProps, Children.None, Null](BookOpenRaw)
  val Loader2    = JsComponent[IconProps, Children.None, Null](Loader2Raw)
  val Play       = JsComponent[IconProps, Children.None, Null](PlayRaw)
  val RotateCcw  = JsComponent[IconProps, Children.None, Null](RotateCcwRaw)
  val Square     = JsComponent[IconProps, Children.None, Null](SquareRaw)
  val Maximize2    = JsComponent[IconProps, Children.None, Null](Maximize2Raw)
  val ZoomIn       = JsComponent[IconProps, Children.None, Null](ZoomInRaw)
  val ZoomOut      = JsComponent[IconProps, Children.None, Null](ZoomOutRaw)
  val Check        = JsComponent[IconProps, Children.None, Null](CheckRaw)
  val Copy         = JsComponent[IconProps, Children.None, Null](CopyRaw)
  val ChevronRight = JsComponent[IconProps, Children.None, Null](ChevronRightRaw)
  val ChevronDown  = JsComponent[IconProps, Children.None, Null](ChevronDownRaw)
  val ListTree     = JsComponent[IconProps, Children.None, Null](ListTreeRaw)
  val ArrowLeft    = JsComponent[IconProps, Children.None, Null](ArrowLeftRaw)
