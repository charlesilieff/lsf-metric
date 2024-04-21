package fr.rebaze.domain.ports.spark.models

case class UserSessionsTime(userId: String, sessionCount: Long, averageSessionTime: Long, firstSession: String, lastSession: Long)
