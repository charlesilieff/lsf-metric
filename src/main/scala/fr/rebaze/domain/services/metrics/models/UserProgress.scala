package fr.rebaze.domain.services.metrics.models

import fr.rebaze.adapters.LevelProgress

//enum UserTenant:
//  case Lsf, Voltaire
case class UserProgress(
  actorGuid: String,
  lastname: Option[String],
  firstname: Option[String],
  completionPercentage: Double,
  levelProgress: Iterable[LevelProgress])
