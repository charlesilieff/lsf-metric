package fr.rebaze.domain.services

import fr.rebaze.domain.services.models.UserProgress
import zio.*

import java.time.LocalDate

trait MetricsService:
  def getUsersProgressByDay(day: LocalDate): Task[Iterable[UserProgress]]
  val extractRulesIdFromJsonDirectExport: Task[Iterable[String]]
  def getGlobalProgressByUserId(userId: String): Task[Double]
  def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): Task[Iterable[String]]
object MetricsService:
  def getUsersProgressByDay(day: LocalDate): RIO[MetricsService, Iterable[UserProgress]] =
    ZIO
      .serviceWithZIO[MetricsService](_.getUsersProgressByDay(day)).tap(value => ZIO.logInfo(s"Metrics for $day and ${value.size} users"))

  val extractRulesIdFromJsonDirectExport: RIO[MetricsService, Iterable[String]] =
    ZIO.serviceWithZIO[MetricsService](_.extractRulesIdFromJsonDirectExport)

  def getGlobalProgressByUserId(userId: String): RIO[MetricsService, Double] =
    ZIO.serviceWithZIO[MetricsService](_.getGlobalProgressByUserId(userId))

  def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): RIO[MetricsService, Iterable[String]] =
    ZIO.serviceWithZIO[MetricsService](_.getLevelIdsByUserIdByDay(userId, day))
