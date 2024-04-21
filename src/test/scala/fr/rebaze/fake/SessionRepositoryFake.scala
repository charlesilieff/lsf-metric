package fr.rebaze.fake

import fr.rebaze.domain.ports.models.LevelsProgressByActorGuid
import fr.rebaze.domain.ports.repository.SessionRepository
import fr.rebaze.domain.ports.repository.models.{LevelId, UserLevelsProgressAndRulesAnswers, Session as SessionModel, ActorGuid}
import fr.rebaze.models.UserFirstnameAndLastname
import zio.{Task, ULayer, ZIO, ZLayer}

import java.time.LocalDate

val MILLI_SECONDS_IN_DAY = 86400000

object SessionRepositoryFake:
  val layer: ULayer[SessionRepositoryFake] =
    ZLayer.succeed(SessionRepositoryFake())
final case class SessionRepositoryFake() extends SessionRepository:
  override def getActorGuidsByDay(day: LocalDate): Task[Iterable[ActorGuid]]                    = ???
  override def getAllSessionsByActorGuid(actorGuid: ActorGuid): Task[Iterable[SessionModel]] = ???

  override def getActorsLevelsProgressAndRulesAnswers(actorGuids: Iterable[ActorGuid]): Task[Iterable[UserLevelsProgressAndRulesAnswers]] =
    ???
  override def getUsersNameAndFirstName(actorGuid: ActorGuid): Task[UserFirstnameAndLastname]                                             = ???

  override def getRulesProgressByActorGuid(actorGuid: ActorGuid): Task[LevelsProgressByActorGuid] = actorGuid match
    case ActorGuid("not-trained-user") => ZIO.succeed(LevelsProgressByActorGuid(actorGuid, Map.empty))
    case ActorGuid("100-trained-user") =>
      ZIO.succeed(LevelsProgressByActorGuid(actorGuid, Map(LevelId("1") -> 1.0, LevelId("2") -> 1.0, LevelId("3") -> 1.0)))
    case ActorGuid("50-trained-user")  =>
      ZIO.logInfo("50%").as(LevelsProgressByActorGuid(actorGuid, Map(LevelId("1") -> 0.5, LevelId("2") -> 0.5, LevelId("3") -> 0.5)))
    case ActorGuid("26-trained-user")  =>
      ZIO.logInfo("26%").as(LevelsProgressByActorGuid(actorGuid, Map(LevelId("1") -> 0.3, LevelId("2") -> 0.0, LevelId("3") -> 0.5)))
