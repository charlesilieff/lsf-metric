package fr.rebaze

import fr.rebaze.adapters.SessionRepositoryLive
import fr.rebaze.db.{AppConfig, Configuration, Db, DbConfig}
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import zio.{Config, ZLayer}

import javax.sql.DataSource

object Layer {
  val prodLayer
    : ZLayer[Any, Config.Error, AppConfig with DbConfig with DataSource with Quill.Postgres[CamelCase] with SessionRepositoryLive] =
    Configuration.live >+> DbConfig.live >+> Db.dataSourceLive >+> Db.quillLive >+> SessionRepositoryLive.layer
}
