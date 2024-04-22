package fr.rebaze.domain.ports.repository.models

import java.time.LocalDate
import scala.collection.immutable.SortedMap

case class LevelProgressRepo(levelId: LevelId, completionPercentage: Double, rulesAnswers: Map[RuleId, SortedMap[Long, Boolean]],completionDate:Option[Long])
