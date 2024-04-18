package fr.rebaze

import fr.rebaze.domain.ports.spark.Spark
import zio.ZIO
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

object SparkSpec extends ZIOSpecDefault:
  def spec = suite("Session Repo")(
    test("Spark") {
      // given

      // then
      assertZIO(ZIO.succeed(1))(equalTo(1))
    }
  )
//{"ruleId": "5548443a-34eb-4fd3-80e2-fd3356da4289", "correct": true, "progress": null, "timestamp": 1672914713065, "exerciseId": "cebba45f-af9a-4c8c-b411-bf5aa08a5fd1"}
