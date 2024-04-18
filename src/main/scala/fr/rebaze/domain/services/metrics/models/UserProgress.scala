package fr.rebaze.domain.services.metrics.models

import fr.rebaze.domain.ports.repository.LevelId


//enum UserTenant:
//  case Lsf, Voltaire
case class LevelProgress(levelId: LevelId, completionPercentage: Double)

case class UserProgress(actorGuid: String, completionPercentage: Double, levelProgress: Iterable[LevelProgress])
