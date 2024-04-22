package fr.rebaze.domain.ports.engine

import zio.{RIO, ZIO}

import scala.collection.immutable.SortedMap

trait Engine:
  def isRuleLearned(ruleInteractions: SortedMap[Long, Boolean]): Boolean
object Engine:
  def isRuleLearned(ruleInteractions: SortedMap[Long, Boolean]): RIO[Engine, Boolean] =
    ZIO.serviceWith[Engine](_.isRuleLearned(ruleInteractions))
