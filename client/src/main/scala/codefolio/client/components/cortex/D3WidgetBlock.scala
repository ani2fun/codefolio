package codefolio.client.components.cortex

import codefolio.client.components.cortex.widgets.{
  ArrayTraversal,
  BTreeWalker,
  CacheStampedeSimulator,
  ConsistentHashRing,
  EstimationCalculator,
  HandshakeTimeline,
  HotShardSimulator,
  LatencyScaledTime,
  PartitionSimulator,
  QueueingSimulator,
  ReplicationLagSimulator
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
      case "handshake-timeline" =>
        HandshakeTimeline.Component(HandshakeTimeline.Props(props.payload))
      case "consistent-hash-ring" =>
        ConsistentHashRing.Component(ConsistentHashRing.Props(props.payload))
      case "cache-stampede" =>
        CacheStampedeSimulator.Component(CacheStampedeSimulator.Props(props.payload))
      case "btree-walker" =>
        BTreeWalker.Component(BTreeWalker.Props(props.payload))
      case "replication-lag" =>
        ReplicationLagSimulator.Component(ReplicationLagSimulator.Props(props.payload))
      case "hot-shard" =>
        HotShardSimulator.Component(HotShardSimulator.Props(props.payload))
      case other =>
        <.div(
          ^.className := "d3-widget__error",
          <.p(^.className := "d3-widget__error-title", "Unknown D3 widget"),
          <.p(
            ^.className := "d3-widget__error-message",
            s"""Widget "$other" is not registered. Available widgets: array-traversal, latency-scaled-time, estimation-calculator, partition-simulator, queueing-simulator, handshake-timeline, consistent-hash-ring, cache-stampede, btree-walker, replication-lag, hot-shard."""
          )
        )
  }
