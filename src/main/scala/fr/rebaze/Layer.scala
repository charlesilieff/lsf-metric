package fr.rebaze

import fr.rebaze.adapters.SessionRepositoryLive
import fr.rebaze.db.{Configuration, Db, DbConfig}
import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.domain.services.{MetricsService, MetricsServiceLayer}
import zio.{Config, ZLayer}

object Layer:
  val sessionRepositoryLayer: ZLayer[Any, Config.Error, SessionRepository] =
    Configuration.live >>> DbConfig.live >>> Db.dataSourceLive >>> Db.quillLive >>> SessionRepositoryLive.layer
  val prodLayer: ZLayer[Any, Config.Error, MetricsService]                 =
    sessionRepositoryLayer >>> MetricsServiceLayer.layer
