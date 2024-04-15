package fr.rebaze

import fr.rebaze.adapters.SessionRepositoryLive
import fr.rebaze.domain.ports.SessionRepository
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

object SessionRepositorySpec extends ZIOSpecDefault:
  def spec = suite("Session Repo")(
    test("return NoSuchSessionException when session not found") {
      // given
      val noSuchElementException = SessionRepository.getSessionByActorGuid("123")

      // then
      assertZIO(noSuchElementException)(isNone)
    },
    test("return a session when a guid session found") {
      // given
      val session = SessionRepository.getSessionByActorGuid("9491340@voltaire")

      // then
      assertZIO(session)(isSome)
    }
  ).provide(Layer.prodLayer)
//{"ruleId": "5548443a-34eb-4fd3-80e2-fd3356da4289", "correct": true, "progress": null, "timestamp": 1672914713065, "exerciseId": "cebba45f-af9a-4c8c-b411-bf5aa08a5fd1"}
