package fr.rebaze.domain.services.models

enum UserTenant:
  case Lsf, Voltaire
case class Metric(userId: String, lastname: Option[String], firstname: Option[String], userTenant: UserTenant)
