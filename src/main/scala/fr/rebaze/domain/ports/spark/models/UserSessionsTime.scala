package fr.rebaze.domain.ports.spark.models

import fr.rebaze.domain.ports.repository.models.ActorGuid

case class UserSessionsTime(sessionCount: Long, averageSessionTime: Long, firstSession: String, lastSession: Long)
