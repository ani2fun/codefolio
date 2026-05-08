package codefolio.shared.cortex

import zio.test.*

object FrontmatterSpec extends ZIOSpecDefault:

  override def spec: Spec[Any, Any] = suite("Frontmatter")(
    suite("parse")(
      test("extracts title + summary from a well-formed frontmatter block") {
        val raw = """---
                    |title: Hello
                    |summary: A short body
                    |---
                    |body content""".stripMargin
        val parsed = Frontmatter.parse(raw, fallbackTitle = "Fallback")
        assertTrue(
          parsed.frontmatter.title == "Hello",
          parsed.frontmatter.summary == Some("A short body"),
          parsed.body == "body content"
        )
      },
      test("falls back to fallbackTitle when title: is absent in frontmatter") {
        val raw = """---
                    |summary: hi
                    |---
                    |body""".stripMargin
        val parsed = Frontmatter.parse(raw, fallbackTitle = "Fallback")
        assertTrue(
          parsed.frontmatter.title == "Fallback",
          parsed.frontmatter.summary == Some("hi"),
          parsed.body == "body"
        )
      },
      test("strips matching single or double quotes around a value") {
        val raw = """---
                    |title: "Quoted Title"
                    |summary: 'Quoted Summary'
                    |---
                    |""".stripMargin
        val parsed = Frontmatter.parse(raw, fallbackTitle = "ignored")
        assertTrue(
          parsed.frontmatter.title == "Quoted Title",
          parsed.frontmatter.summary == Some("Quoted Summary")
        )
      },
      test("treats unterminated frontmatter as plain body (ADR-0001)") {
        val raw = """---
                    |title: Should Not Be Parsed
                    |summary: Also ignored
                    |
                    |Body content with no closing fence.
                    |""".stripMargin
        val parsed = Frontmatter.parse(raw, fallbackTitle = "Fallback")
        assertTrue(
          parsed.frontmatter.title == "Fallback",
          parsed.frontmatter.summary.isEmpty,
          parsed.body == raw
        )
      },
      test("body without leading frontmatter fence is returned verbatim with fallback title") {
        val parsed = Frontmatter.parse("# Heading\n\nbody", fallbackTitle = "Fallback")
        assertTrue(
          parsed.frontmatter.title == "Fallback",
          parsed.frontmatter.summary.isEmpty,
          parsed.body == "# Heading\n\nbody"
        )
      },
      test("empty content yields fallback frontmatter and empty body") {
        val parsed = Frontmatter.parse("", fallbackTitle = "Fallback")
        assertTrue(
          parsed.frontmatter.title == "Fallback",
          parsed.frontmatter.summary.isEmpty,
          parsed.body == ""
        )
      }
    ),
    suite("extractTitle")(
      test("frontmatter title wins over body H1 and over fallback") {
        val raw = """---
                    |title: Frontmatter Title
                    |---
                    |# H1 Title""".stripMargin
        assertTrue(Frontmatter.extractTitle(raw, fallback = "Fallback") == "Frontmatter Title")
      },
      test("falls back to first H1 when no frontmatter title") {
        val raw = "# H1 Title\n\nbody"
        assertTrue(Frontmatter.extractTitle(raw, fallback = "Fallback") == "H1 Title")
      },
      test("falls back through to fallback when neither frontmatter nor H1 is present") {
        val raw = "body without heading"
        assertTrue(Frontmatter.extractTitle(raw, fallback = "Fallback") == "Fallback")
      },
      test("an empty H1 line is ignored") {
        val raw = "# \n\nbody"
        assertTrue(Frontmatter.extractTitle(raw, fallback = "Fallback") == "Fallback")
      },
      test("ignores headings that appear inside a well-formed frontmatter region") {
        val raw = """---
                    |---
                    |# After Frontmatter""".stripMargin
        assertTrue(Frontmatter.extractTitle(raw, fallback = "Fallback") == "After Frontmatter")
      }
    )
  )
