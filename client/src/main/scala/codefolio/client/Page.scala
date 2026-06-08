package codefolio.client

/** All routes the SPA knows about. The portfolio is a single-page site: the only route is the home page. */
sealed trait Page

object Page:
  case object Home extends Page
