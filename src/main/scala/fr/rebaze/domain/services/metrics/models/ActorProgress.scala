package fr.rebaze.domain.services.metrics.models

import fr.rebaze.domain.ports.repository.models.*

//enum UserTenant:
//  case Lsf, Voltaire
case class LevelProgress(levelId: LevelId, completionPercentage: Double, rules: Map[RuleId, Boolean],completionDate:Option[Long])

case class ActorProgress(completionPercentage: Double, levelProgress: Iterable[LevelProgress], knownRulesNbr: Int, totalRulesNbr: Int)
