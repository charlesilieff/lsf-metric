package fr.rebaze.domain.services

import fr.rebaze.domain.services.models.Metric
import zio.*

import java.time.LocalDate

trait MetricsService:
  def getMetricsByDay(day: LocalDate): Task[Seq[Metric]]
  def extractRulesIdFromJsonDirectExport(path: String): Task[Seq[String]]
object MetricsService:
  def getMetricsByDay(day: LocalDate): RIO[MetricsService, Seq[Metric]] =
    ZIO.serviceWithZIO[MetricsService](_.getMetricsByDay(day))

  def extractRulesIdFromJsonDirectExport(path: String): RIO[MetricsService, Seq[String]] =
    ZIO.serviceWithZIO[MetricsService](_.extractRulesIdFromJsonDirectExport(path))
