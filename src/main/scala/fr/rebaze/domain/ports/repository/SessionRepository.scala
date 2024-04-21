package fr.rebaze.domain.ports.repository

import fr.rebaze.domain.ports.models.LevelsProgressByActorGuid
import fr.rebaze.domain.ports.repository.models.*
import fr.rebaze.models.UserFirstnameAndLastname
import zio.*

import java.time.LocalDate

trait SessionRepository:
  def getAllSessionsByActorGuid(actorGuid: ActorGuid): Task[Iterable[Session]]
  def getActorsLevelsProgressAndRulesAnswers(actorGuids: Iterable[ActorGuid]): Task[Iterable[UserLevelsProgressAndRulesAnswers]]
  def getUsersNameAndFirstName(actorGuid: ActorGuid): Task[UserFirstnameAndLastname]
  def getRulesProgressByActorGuid(actorGuid: ActorGuid): Task[LevelsProgressByActorGuid]
  def getActorGuidsByDay(day: LocalDate): Task[Iterable[ActorGuid]]
object SessionRepository:
  def getAllSessionsByActorGuid(actorGuid: ActorGuid): RIO[SessionRepository, Iterable[Session]]                                    =
    ZIO.serviceWithZIO[SessionRepository](_.getAllSessionsByActorGuid(actorGuid))
  def getActorsLevelsProgressAndRulesAnswers(actorGuids: List[ActorGuid]): RIO[SessionRepository, Iterable[UserLevelsProgressAndRulesAnswers]] =
    ZIO.serviceWithZIO[SessionRepository](_.getActorsLevelsProgressAndRulesAnswers(actorGuids))

  def getUsersNameAndFirstName(actorGuid: ActorGuid): RIO[SessionRepository, UserFirstnameAndLastname] =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersNameAndFirstName(actorGuid))

  def getRulesProgressByActorGuid(actorGuid: ActorGuid): RIO[SessionRepository, LevelsProgressByActorGuid] =
    ZIO.serviceWithZIO[SessionRepository](_.getRulesProgressByActorGuid(actorGuid))
  def getActorGuidsByDay(day: LocalDate): RIO[SessionRepository, Iterable[ActorGuid]]                   =
    ZIO.serviceWithZIO[SessionRepository](_.getActorGuidsByDay(day))
