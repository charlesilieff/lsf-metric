package fr.rebaze

import fr.rebaze.common.Layer
import fr.rebaze.domain.services.metrics.MetricsService
import zio.*
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

import java.time.LocalDate

object MetricsServiceSpec extends ZIOSpecDefault:
  def spec = suite("Metrics service")(
    test("get metrics by day") {
      // given
      val metrics = MetricsService
        .getUsersProgressByDay(LocalDate.of(2024, 1, 16)).tap(values =>
          ZIO.logInfo(s"Metrics: ${values.filter(metric => !metric.actorGuid.contains("@voltaire"))}"))

      // then
      assertZIO(metrics)(isNonEmpty)
    },
    test("read all rules id") {
      // given
      val metrics = MetricsService
        .extractRulesIdFromJsonDirectExport.tap(values => ZIO.logInfo(s"Metrics: ${values}"))

      // then
      assertZIO(metrics)(isNonEmpty)
    },
    test("get global average progress for userId, this user has only progress on non existing rules") {
      val userId  = "charles@voltaire"
      val metrics = MetricsService
        .getGlobalProgressByUserId(userId).tap(values => ZIO.logInfo(s"Metrics: ${values}"))

      // then
      assertZIO(metrics)(equalTo(0.0))
    },
    test("get global average progress for userId") {
      val userId  = "anthony.b@rebaze.fr@lsf"
      val metrics = MetricsService
        .getGlobalProgressByUserId(userId).tap(values => ZIO.logInfo(s"Metrics: ${values}"))

      // then
      assertZIO(metrics)(equalTo(0.0013433508640120209))
    }
  ).provide(Layer.prodLayer)
//{"ruleId": "5548443a-34eb-4fd3-80e2-fd3356da4289", "correct": true, "progress": null, "timestamp": 1672914713065, "exerciseId": "cebba45f-af9a-4c8c-b411-bf5aa08a5fd1"}
