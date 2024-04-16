package fr.rebaze.domain.services

import fr.rebaze.domain.services.models.Metric
import zio.*

import java.time.LocalDate

trait MetricsService:
  def getMetricsByDay(day: LocalDate): Task[Seq[Metric]]
  val extractRulesIdFromJsonDirectExport: Task[Seq[String]]
  def getGlobalProgressByUserId(userId: String): Task[Double]
object MetricsService:
  def getMetricsByDay(day: LocalDate): RIO[MetricsService, Seq[Metric]] =
    ZIO.serviceWithZIO[MetricsService](_.getMetricsByDay(day))

  val extractRulesIdFromJsonDirectExport: RIO[MetricsService, Seq[String]] =
    ZIO.serviceWithZIO[MetricsService](_.extractRulesIdFromJsonDirectExport)

  def getGlobalProgressByUserId(userId: String): RIO[MetricsService, Double] =
    ZIO.serviceWithZIO[MetricsService](_.getGlobalProgressByUserId(userId))
