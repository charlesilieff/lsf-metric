package fr.rebaze

import fr.rebaze.adapters.SessionRepositoryLive
import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.models.UserFirstnameAndLastname
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

import java.time.LocalDate

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
    },
    test("return all users in a day") {
      // given
      val users = SessionRepository.getUsersByDay(LocalDate.of(1999, 1, 1))

      // then
      assertZIO(users)(isEmpty)
    },
    test("return all users in a day 16/01/2024, it should be more than 100") {
      // given
      val users = SessionRepository.getUsersByDay(LocalDate.of(2024, 1, 16))

      // then
      assertZIO(users)(isNonEmpty)
    },
    test("Find firstname and lastname") {
      // given
      val userId = "anthony.b@rebaze.fr@lsf"
      val user   = SessionRepository.getUsersNameAndFirstName(userId)

      // then
      assertZIO(user)(
        equalTo(
          UserFirstnameAndLastname(
            lastname = Some("Benier"),
            firstname = Some("Anthony")
          )))
    }
  ).provide(Layer.prodLayer)
//{"ruleId": "5548443a-34eb-4fd3-80e2-fd3356da4289", "correct": true, "progress": null, "timestamp": 1672914713065, "exerciseId": "cebba45f-af9a-4c8c-b411-bf5aa08a5fd1"}