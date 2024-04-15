package fr.rebaze

import fr.rebaze.adapters.SessionRepositoryLive
import fr.rebaze.db.{AppConfig, Configuration, Db, DbConfig}
import io.getquill.CamelCase
import io.getquill.jdbczio.Quill
import zio.{Config, ZLayer}

import javax.sql.DataSource

object Layer:
  val prodLayer: ZLayer[Any, Config.Error, AppConfig & DbConfig & DataSource & Quill.Postgres[CamelCase] & SessionRepositoryLive] =
    Configuration.live >+> DbConfig.live >+> Db.dataSourceLive >+> Db.quillLive >+> SessionRepositoryLive.layer