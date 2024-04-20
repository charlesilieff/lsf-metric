package fr.rebaze.domain.services.metrics.models

import fr.rebaze.domain.ports.repository.models.{LevelId, RuleId}

//enum UserTenant:
//  case Lsf, Voltaire
case class LevelProgress(levelId: LevelId, completionPercentage: Double, rules: Map[RuleId, Boolean])

case class UserProgress(actorGuid: String, completionPercentage: Double, levelProgress: Iterable[LevelProgress])
