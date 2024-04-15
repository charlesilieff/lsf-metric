package fr.rebaze

import fr.rebaze.adapters.SessionRepositoryLive
import fr.rebaze.domain.services.{MetricsService, MetricsServiceLayer}
import zio.*
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

import java.time.LocalDate

object MetricsServiceSpec extends ZIOSpecDefault:
  def spec = suite("Metrics service")(
    test("get metrics by day") {
      // given
      val metrics = MetricsService
        .getMetricsByDay(LocalDate.of(2024, 1, 16)).tap(values =>
          ZIO.logInfo(s"Metrics: ${values.filter(metric => !metric.userId.contains("@voltaire"))}"))

      // then
      assertZIO(metrics)(isNonEmpty)
    }
  ).provide(Layer.prodLayer, MetricsServiceLayer.layer)
//{"ruleId": "5548443a-34eb-4fd3-80e2-fd3356da4289", "correct": true, "progress": null, "timestamp": 1672914713065, "exerciseId": "cebba45f-af9a-4c8c-b411-bf5aa08a5fd1"}
