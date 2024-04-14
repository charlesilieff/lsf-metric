package fr.rebaze.db

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.*
import io.getquill.jdbczio.*
import zio.{ZIO, ZLayer}

import javax.sql.DataSource

object Db:
  private def create(dbConfig: DbConfig): HikariDataSource = {
    val poolConfig = new HikariConfig()
    poolConfig.setUsername(dbConfig.user)
    poolConfig.setPassword(dbConfig.password)
    poolConfig.setSchema(dbConfig.schema)
    poolConfig.addDataSourceProperty("databaseName", "chartres_cms");
    poolConfig.addDataSourceProperty("portNumber", 35432);
    poolConfig.addDataSourceProperty("serverName", "localhost");
    poolConfig.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource")
    new HikariDataSource(poolConfig)
  }

  // Used for migration and executing queries.
  val dataSourceLive: ZLayer[DbConfig, Nothing, DataSource] =
    ZLayer.scoped {
      ZIO.fromAutoCloseable {
        for {
          dbConfig   <- ZIO.service[DbConfig]
          dataSource <- ZIO.succeed(create(dbConfig))
        } yield dataSource
      }
    }

  // Quill framework object used for specifying sql queries.
  val quillLive: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)
