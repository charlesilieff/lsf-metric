package fr.rebaze.domain.ports.repository.models

import scala.collection.immutable.SortedMap

case class LevelProgressRepo(levelId: LevelId, completionPercentage: Double, rulesAnswers: Map[RuleId, SortedMap[Long, Boolean]])
