package fr.rebaze.domain.ports.engine

import zio.{RIO, Task, ZIO}

import java.time.LocalDateTime
import scala.collection.immutable.SortedMap

trait Engine:
  def isRuleLearned(ruleInteractions: SortedMap[Long, Boolean]): Task[Boolean]
object Engine:
  def isRuleLearned(ruleInteractions: SortedMap[Long, Boolean]): RIO[Engine, Boolean] =
    ZIO.serviceWithZIO[Engine](_.isRuleLearned(ruleInteractions))
