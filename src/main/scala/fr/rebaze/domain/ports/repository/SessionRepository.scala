package fr.rebaze.domain.ports.repository

import fr.rebaze.domain.ports.models.RulesProgressByUserId
import fr.rebaze.domain.ports.repository.models.*
import fr.rebaze.models.UserFirstnameAndLastname
import zio.*

import java.time.LocalDate

trait SessionRepository:
  def getAllSessionsByActorGuid(guid: String): Task[Iterable[Session]]
  def getUsersLevelsProgressAndRulesAnswers(day: LocalDate): Task[Iterable[UserLevelsProgressAndRulesAnswers]]
  def getUsersNameAndFirstName(userId: String): Task[UserFirstnameAndLastname]
  def getRulesProgressByUserId(userId: String): Task[RulesProgressByUserId]
  def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): Task[Iterable[String]]
object SessionRepository:
  def getAllSessionsByActorGuid(guid: String): RIO[SessionRepository, Iterable[Session]]                                         =
    ZIO.serviceWithZIO[SessionRepository](_.getAllSessionsByActorGuid(guid))
  def getUsersLevelsProgressAndRulesAnswers(day: LocalDate): RIO[SessionRepository, Iterable[UserLevelsProgressAndRulesAnswers]] =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersLevelsProgressAndRulesAnswers(day))

  def getUsersNameAndFirstName(userId: String): RIO[SessionRepository, UserFirstnameAndLastname] =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersNameAndFirstName(userId))

  def getRulesProgressByUserId(userId: String): RIO[SessionRepository, RulesProgressByUserId]            =
    ZIO.serviceWithZIO[SessionRepository](_.getRulesProgressByUserId(userId))
  def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): RIO[SessionRepository, Iterable[String]] =
    ZIO.serviceWithZIO[SessionRepository](_.getLevelIdsByUserIdByDay(userId, day))
