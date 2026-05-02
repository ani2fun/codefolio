package codefolio.client

import codefolio.client.components.Hello
import japgolly.scalajs.react.ReactDOMClient
import org.scalajs.dom

object Main:

  def main(args: Array[String]): Unit =
    val container = dom.document.getElementById("root")
    if container != null then
      val root = ReactDOMClient.createRoot(container)
      root.render(Hello.Component())
