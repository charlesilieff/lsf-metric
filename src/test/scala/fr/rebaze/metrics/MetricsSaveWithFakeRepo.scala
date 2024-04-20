package fr.rebaze.metrics

import fr.rebaze.domain.ports.engine.{Engine, EngineLive}
import fr.rebaze.domain.services.metrics.{MetricsService, MetricsServiceLive}
import fr.rebaze.fake.SessionRepositoryFake
import zio.*
import zio.nio.file.Path
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

object MetricsServiceSpecWithFakeRepo extends ZIOSpecDefault:
  def spec = suite("Metrics service with fake repo")(
    test("There is three rules in tests rules") {
      // given
      val rulesCount = MetricsService.extractRulesIdFromJsonDirectExport(Path("/src/main/resources/rules/tests/")).map(_.size)

      // then s
      assertZIO(rulesCount)(equalTo(3))
    },
    test("Get global progress user has not trained") {
      // given
      val globalProgress = MetricsService.getGlobalProgressByActorGuid("not-trained-user", Path("/src/main/resources/rules/tests/"))

      // then s
      assertZIO(globalProgress)(equalTo(0.0))
    },
    test("Get global progress user 100% trained") {
      // given
      val globalProgress = MetricsService.getGlobalProgressByActorGuid("100-trained-user", Path("/src/main/resources/rules/tests/"))

      // then s
      assertZIO(globalProgress)(equalTo(1.0))
    }
  ).provide(MetricsServiceLive.layer, EngineLive.layer, SessionRepositoryFake.layer)
//{"ruleId": "5548443a-34eb-4fd3-80e2-fd3356da4289", "correct": true, "progress": null, "timestamp": 1672914713065, "exerciseId": "cebba45f-af9a-4c8c-b411-bf5aa08a5fd1"}
