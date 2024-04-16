package fr.rebaze.domain.services

import fr.rebaze.domain.services.models.UserProgress
import zio.*

import java.time.LocalDate

trait MetricsService:
  def getMetricsByDay(day: LocalDate): Task[Seq[UserProgress]]
  val extractRulesIdFromJsonDirectExport: Task[Seq[String]]
  def getGlobalProgressByUserId(userId: String): Task[Double]
object MetricsService:
  def getMetricsByDay(day: LocalDate): RIO[MetricsService, Seq[UserProgress]] =
    ZIO.serviceWithZIO[MetricsService](_.getMetricsByDay(day)).tap(value => ZIO.logInfo(s"Metrics for $day and ${value.length} users"))

  val extractRulesIdFromJsonDirectExport: RIO[MetricsService, Seq[String]] =
    ZIO.serviceWithZIO[MetricsService](_.extractRulesIdFromJsonDirectExport)

  def getGlobalProgressByUserId(userId: String): RIO[MetricsService, Double] =
    ZIO.serviceWithZIO[MetricsService](_.getGlobalProgressByUserId(userId))
