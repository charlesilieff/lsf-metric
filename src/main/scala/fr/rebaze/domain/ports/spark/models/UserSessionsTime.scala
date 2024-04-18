package fr.rebaze.domain.ports.spark.models

import scala.concurrent.duration.Duration

case class UserSessionsTime(userId: String, sessionCount: Long, averageSessionTime: Duration, firstSession: String, lastSession: Long)
