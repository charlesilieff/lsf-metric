package fr.rebaze.domain.services.engine

import zio.{RIO, Task, ZIO}

import java.time.LocalDateTime

trait EngineService:
  def isRuleLearned(ruleInteractions: Map[LocalDateTime, Boolean]): Task[Boolean]
object EngineService:
  def isRuleLearned(ruleInteractions: Map[LocalDateTime, Boolean]): RIO[EngineService, Boolean] =
    ZIO.serviceWithZIO[EngineService](_.isRuleLearned(ruleInteractions))
