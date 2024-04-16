package fr.rebaze.db

import zio.config.magnolia._
import zio.config.typesafe.TypesafeConfigProvider
import zio.{ZLayer, _}

final case class RootConfig(
  config: AppConfig
)
final case class AppConfig(
  system: SystemConfig,
  db: DatabaseConfig
)

final case class SystemConfig(
  jwtSecret: String
)

final case class DatabaseConfig(
  user: String,
  password: String,
  port: Int,
  database: String,
  schema: String,
  serverName: String
)

object Configuration {
  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(TypesafeConfigProvider.fromResourcePath().load(deriveConfig[RootConfig]).map(_.config))
}
