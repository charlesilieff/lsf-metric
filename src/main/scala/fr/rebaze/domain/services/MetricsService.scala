package fr.rebaze.domain.services

import fr.rebaze.domain.services.models.Metric
import zio.*

import java.time.LocalDate

trait MetricsService:
  def getMetricsByDay(day: LocalDate): Task[Seq[Metric]]
object MetricsService:
  def getMetricsByDay(day: LocalDate): RIO[MetricsService, Seq[Metric]] =
    ZIO.serviceWithZIO[MetricsService](_.getMetricsByDay(day))
