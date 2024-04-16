package fr.rebaze.db

import zio.{ZIO, ZLayer}

case class DbConfig(user: String, password: String, port: Int, database: String, schema: String,serverName:String)

object DbConfig:
  val live: ZLayer[AppConfig, Nothing, DbConfig] =
    ZLayer.fromZIO {
      for {
        appConfig <- ZIO.service[AppConfig]
      } yield DbConfig(appConfig.db.user, appConfig.db.password, appConfig.db.port, appConfig.db.database, appConfig.db.schema, appConfig.db.serverName)
    }
