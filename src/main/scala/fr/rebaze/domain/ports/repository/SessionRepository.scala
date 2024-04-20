package fr.rebaze.domain.ports.repository

import fr.rebaze.domain.ports.models.LevelsProgressByUserId
import fr.rebaze.domain.ports.repository.models.*
import fr.rebaze.models.UserFirstnameAndLastname
import zio.*

import java.time.LocalDate

trait SessionRepository:
  def getAllSessionsByActorGuid(actorGuid: String): Task[Iterable[Session]]
  def getUsersLevelsProgressAndRulesAnswers(day: LocalDate): Task[Iterable[UserLevelsProgressAndRulesAnswers]]
  def getUsersNameAndFirstName(actorGuid: String): Task[UserFirstnameAndLastname]
  def getRulesProgressByActorGuid(actorGuid: String): Task[LevelsProgressByUserId]
object SessionRepository:
  def getAllSessionsByActorGuid(actorGuid: String): RIO[SessionRepository, Iterable[Session]]                                    =
    ZIO.serviceWithZIO[SessionRepository](_.getAllSessionsByActorGuid(actorGuid))
  def getUsersLevelsProgressAndRulesAnswers(day: LocalDate): RIO[SessionRepository, Iterable[UserLevelsProgressAndRulesAnswers]] =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersLevelsProgressAndRulesAnswers(day))

  def getUsersNameAndFirstName(actorGuid: String): RIO[SessionRepository, UserFirstnameAndLastname] =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersNameAndFirstName(actorGuid))

  def getRulesProgressByActorGuid(actorGuid: String): RIO[SessionRepository, LevelsProgressByUserId] =
    ZIO.serviceWithZIO[SessionRepository](_.getRulesProgressByActorGuid(actorGuid))
