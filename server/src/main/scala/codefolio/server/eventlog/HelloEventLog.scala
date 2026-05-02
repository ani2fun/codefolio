package codefolio.server.eventlog

import codefolio.server.config.AppConfig
import codefolio.shared.api.Endpoints.HelloEvent
import com.mongodb.client.model.{IndexOptions, Indexes, Sorts}
import com.mongodb.client.{MongoClient, MongoClients, MongoCollection}
import org.bson.Document
import zio.*

/** Append-only log of every /api/hello call, persisted in MongoDB. */
trait HelloEventLog:
  def append(event: HelloEvent): Task[Unit]
  def recent(limit: Int): Task[List[HelloEvent]]
  def ping: Task[Boolean]

object HelloEventLog:

  private val CollectionName = "hello_events"

  val live: ZLayer[AppConfig, Throwable, HelloEventLog] =
    ZLayer.scoped {
      for
        cfg    <- ZIO.service[AppConfig]
        client <- ZIO.acquireRelease(
                    ZIO.attemptBlocking(MongoClients.create(cfg.mongo.uri))
                  )(c => ZIO.attempt(c.close()).orDie)
        coll   <- ZIO.attemptBlocking {
                    val db   = client.getDatabase(cfg.mongo.database)
                    val coll = db.getCollection(CollectionName)
                    // Idempotent: ensures we can scan "newest first" cheaply.
                    coll.createIndex(
                      Indexes.descending("timestampEpochMs"),
                      IndexOptions().name("ts_desc")
                    )
                    coll
                  }
      yield HelloEventLogLive(client, coll)
    }

private final class HelloEventLogLive(
  client: MongoClient,
  coll: MongoCollection[Document]
) extends HelloEventLog:

  override def append(event: HelloEvent): Task[Unit] = ZIO.attemptBlocking {
    val doc = Document()
      .append("timestampEpochMs", java.lang.Long.valueOf(event.timestampEpochMs))
      .append("visits", java.lang.Long.valueOf(event.visits))
    coll.insertOne(doc)
    ()
  }

  override def recent(limit: Int): Task[List[HelloEvent]] = ZIO.attemptBlocking {
    val cursor =
      coll
        .find()
        .sort(Sorts.descending("timestampEpochMs"))
        .limit(limit)
        .iterator()
    try
      val buf = List.newBuilder[HelloEvent]
      while cursor.hasNext do
        val d = cursor.next()
        buf += HelloEvent(
          timestampEpochMs = d.getLong("timestampEpochMs"),
          visits = d.getLong("visits")
        )
      buf.result()
    finally cursor.close()
  }

  override def ping: Task[Boolean] =
    ZIO
      .attemptBlocking(client.getDatabase("admin").runCommand(Document("ping", 1)))
      .as(true)
      .catchAll(_ => ZIO.succeed(false))
