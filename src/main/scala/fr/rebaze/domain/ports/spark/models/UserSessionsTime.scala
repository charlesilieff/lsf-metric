package fr.rebaze.domain.ports.spark.models

case class UserSessionsTime(sessionCount: Long, averageSessionTime: Long, firstSession: String, lastSession: Long)
