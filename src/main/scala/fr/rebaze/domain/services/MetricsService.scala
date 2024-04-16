package fr.rebaze.domain.services

import fr.rebaze.domain.services.models.UserProgress
import zio.*

import java.time.LocalDate

trait MetricsService:
  def getUsersGlobalProgressByDay(day: LocalDate): Task[Seq[UserProgress]]
  val extractRulesIdFromJsonDirectExport: Task[Seq[String]]
  def getGlobalProgressByUserId(userId: String): Task[Double]
  def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): Task[Seq[String]]
object MetricsService:
  def getUsersGlobalProgressByDay(day: LocalDate): RIO[MetricsService, Seq[UserProgress]] =
    ZIO
      .serviceWithZIO[MetricsService](_.getUsersGlobalProgressByDay(day)).tap(value =>
        ZIO.logInfo(s"Metrics for $day and ${value.length} users"))

  val extractRulesIdFromJsonDirectExport: RIO[MetricsService, Seq[String]] =
    ZIO.serviceWithZIO[MetricsService](_.extractRulesIdFromJsonDirectExport)

  def getGlobalProgressByUserId(userId: String): RIO[MetricsService, Double] =
    ZIO.serviceWithZIO[MetricsService](_.getGlobalProgressByUserId(userId))

  def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): RIO[MetricsService, Seq[String]] =
    ZIO.serviceWithZIO[MetricsService](_.getLevelIdsByUserIdByDay(userId, day))
