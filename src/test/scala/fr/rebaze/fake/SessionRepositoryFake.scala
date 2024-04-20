package fr.rebaze.fake

import fr.rebaze.domain.ports.models.LevelsProgressByUserId
import fr.rebaze.domain.ports.repository.SessionRepository
import fr.rebaze.domain.ports.repository.models.{LevelId, UserLevelsProgressAndRulesAnswers, Session as SessionModel}
import fr.rebaze.models.UserFirstnameAndLastname
import zio.{Task, ULayer, ZIO, ZLayer}

import java.time.LocalDate

val MILLI_SECONDS_IN_DAY = 86400000

object SessionRepositoryFake:
  val layer: ULayer[SessionRepositoryFake] =
    ZLayer.succeed(SessionRepositoryFake())
final case class SessionRepositoryFake() extends SessionRepository:
  override def getAllSessionsByActorGuid(actorGuid: String): Task[Iterable[SessionModel]] = ???

  override def getUsersLevelsProgressAndRulesAnswers(day: LocalDate): Task[Iterable[UserLevelsProgressAndRulesAnswers]] = ???

  override def getUsersNameAndFirstName(actorGuid: String): Task[UserFirstnameAndLastname] = ???

  override def getRulesProgressByActorGuid(actorGuid: String): Task[LevelsProgressByUserId] = actorGuid match
    case "not-trained-user" => ZIO.succeed(LevelsProgressByUserId(actorGuid, Map.empty))
    case "100-trained-user" =>
      ZIO.succeed(LevelsProgressByUserId(actorGuid, Map(LevelId("1") -> 1.0, LevelId("2") -> 1.0, LevelId("3") -> 1.0)))
