package fr.rebaze.domain.services.spark.models

import scala.concurrent.duration.Duration

case class UserSessionsTime(userId: String, sessionCount: Long, averageSessionTime: Duration, firstSession: String, lastSession: String)
