package fr.rebaze.domain.services.models

enum UserTenant:
  case Lsf, Voltaire
case class UserProgress(
  actorGuid: String,
  lastname: Option[String],
  firstname: Option[String],
  userTenant: UserTenant,
  globalProgress: Double)
