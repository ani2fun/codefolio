package codefolio.client.data

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/** Static portfolio data — projects, work history, certifications.
  *
  * The JSON files live next to this binding in `client/src/data/`. A small
  * TypeScript bridge (`portfolioData.ts`) re-exports them so Vite inlines
  * them as ES-module bindings at bundle time. The `@data` alias is wired in
  * `vite.config.mjs` so the path stays stable regardless of linker output
  * layout.
  *
  * Each shape is a `js.Object` trait — fields are read directly off the
  * deserialised JSON, no glue conversion needed. Add a new field to the JSON
  * and a corresponding `val` here; everything else is type-checked.
  */
object PortfolioData:

  // ---- Projects -----------------------------------------------------------

  @js.native trait ProjectImage extends js.Object:
    val url: String = js.native
    val alt: String = js.native

  @js.native trait Project extends js.Object:
    val name: String                  = js.native
    val tags: js.Array[String]        = js.native
    val projectUrl: String            = js.native
    val githubUrl: String             = js.native
    val image: ProjectImage           = js.native
    val description: String           = js.native

  // ---- Experience ---------------------------------------------------------

  @js.native trait Company extends js.Object:
    val short: String = js.native
    val name: String  = js.native
    val url: String   = js.native

  @js.native trait Experience extends js.Object:
    val position: String                      = js.native
    val company: Company                      = js.native
    val time: String                          = js.native
    val description: String                   = js.native
    val items: js.Array[String]               = js.native
    val results: js.UndefOr[js.Array[String]] = js.native
    val leveragedKnowledgeIn: js.Array[String] = js.native

  // ---- Certifications -----------------------------------------------------

  @js.native trait Certification extends js.Object:
    val name: String           = js.native
    val issuer: String         = js.native
    val date: String           = js.native
    val length: String         = js.native
    val description: String    = js.native
    val tags: js.Array[String] = js.native
    val url: String            = js.native
    val highlight: Boolean     = js.native

  // ---- Module bindings ----------------------------------------------------

  @js.native @JSImport("@data/portfolioData", "projects")
  val projects: js.Array[Project] = js.native

  @js.native @JSImport("@data/portfolioData", "experience")
  val experience: js.Array[Experience] = js.native

  @js.native @JSImport("@data/portfolioData", "certifications")
  val certifications: js.Array[Certification] = js.native
