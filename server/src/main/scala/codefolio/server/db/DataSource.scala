package codefolio.server.db

import codefolio.server.config.AppConfig
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import zio.*

import javax.sql.DataSource as JDataSource

object DataSource:

  val live: ZLayer[AppConfig, Throwable, JDataSource] =
    ZLayer.scoped {
      for
        cfg <- ZIO.service[AppConfig]
        ds <- ZIO.acquireRelease(
          ZIO.attemptBlocking {
            val hc: HikariConfig = HikariConfig()
            hc.setJdbcUrl(cfg.db.url)
            hc.setUsername(cfg.db.user)
            hc.setPassword(cfg.db.password)
            hc.setMaximumPoolSize(10)
            hc.setPoolName("codefolio-pool")
            HikariDataSource(hc)
          }
        )(ds => ZIO.attempt(ds.close()).orDie)
      yield ds: JDataSource
    }
