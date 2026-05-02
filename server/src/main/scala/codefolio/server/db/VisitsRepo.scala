package codefolio.server.db

import zio.*

import javax.sql.DataSource as JDataSource

trait VisitsRepo:
  def incrementAndGet: Task[Long]

object VisitsRepo:

  val live: ZLayer[JDataSource, Nothing, VisitsRepo] =
    ZLayer.fromFunction(VisitsRepoLive(_))

final private class VisitsRepoLive(ds: JDataSource) extends VisitsRepo:

  override def incrementAndGet: Task[Long] = ZIO.attemptBlocking {
    val conn = ds.getConnection
    try
      conn.setAutoCommit(false)
      val stmt = conn.prepareStatement(
        "UPDATE visits SET count = count + 1 WHERE id = 1 RETURNING count"
      )
      try
        val rs = stmt.executeQuery()
        try
          val n = if rs.next() then rs.getLong(1) else 0L
          conn.commit()
          n
        finally rs.close()
      finally stmt.close()
    finally conn.close()
  }
