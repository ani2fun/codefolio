package codefolio.client.components.cortex

import codefolio.client.components.cortex.widgets.{
  ArrayTraversal,
  EstimationCalculator,
  LatencyScaledTime,
  PartitionSimulator,
  QueueingSimulator
}
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*

/**
 * Runtime dispatcher for `Block.D3Widget(name, payload)`. The widget catalog is a closed map from widget-name
 * → component; an unknown name renders an inline error so the rest of the chapter still mounts.
 *
 * New widgets land here as new cases. The structural decode in `shared.cortex.Blocks` is deliberately loose —
 * each widget owns the schema of its payload — so growing the catalog never touches shared.
 */
object D3WidgetBlock:

  final case class Props(widget: String, payload: String)

  val Component = ScalaFnComponent[Props] { props =>
    props.widget match
      case "array-traversal" =>
        ArrayTraversal.Component(ArrayTraversal.Props(props.payload))
      case "latency-scaled-time" =>
        LatencyScaledTime.Component(LatencyScaledTime.Props(props.payload))
      case "estimation-calculator" =>
        EstimationCalculator.Component(EstimationCalculator.Props(props.payload))
      case "partition-simulator" =>
        PartitionSimulator.Component(PartitionSimulator.Props(props.payload))
      case "queueing-simulator" =>
        QueueingSimulator.Component(QueueingSimulator.Props(props.payload))
      case other =>
        <.div(
          ^.className := "d3-widget__error",
          <.p(^.className := "d3-widget__error-title", "Unknown D3 widget"),
          <.p(
            ^.className := "d3-widget__error-message",
            s"""Widget "$other" is not registered. Available widgets: array-traversal, latency-scaled-time, estimation-calculator, partition-simulator, queueing-simulator."""
          )
        )
  }
