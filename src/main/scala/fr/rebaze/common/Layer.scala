package fr.rebaze.common

import fr.rebaze.domain.ports.engine.*
import fr.rebaze.domain.ports.repository.*
import fr.rebaze.domain.services.metrics.MetricsService
import fr.rebaze.domain.services.metrics.MetricsServiceLive
import zio.{Config, ZLayer}

object Layer:
  val sessionRepositoryLayer: ZLayer[Any, Config.Error, SessionRepository & Engine] =
    Configuration.live >>> DbConfig.live >>> Db.dataSourceLive >>> Db.quillLive >>> SessionRepositoryLive.layer ++ EngineLive.layer
  val prodLayer: ZLayer[Any, Config.Error, MetricsService]                          =
    sessionRepositoryLayer >>> MetricsServiceLive.layer
