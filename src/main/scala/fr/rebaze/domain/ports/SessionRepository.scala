package fr.rebaze.domain.ports

import fr.rebaze.domain.ports.models.RulesProgressByUserId
import fr.rebaze.models.{Session, UserFirstnameAndLastname, UserWithRules}
import zio.*

import java.time.LocalDate

trait SessionRepository:
  def getAllSessionsByActorGuid(guid: String): Task[Iterable[Session]]
  def getUsersWithRulesTrainedByDay(day: LocalDate): Task[Iterable[UserWithRules]]
  def getUsersNameAndFirstName(userId: String): Task[UserFirstnameAndLastname]
  def getRulesProgressByUserId(userId: String): Task[RulesProgressByUserId]
  def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): Task[Iterable[String]]
object SessionRepository:
  def getAllSessionsByActorGuid(guid: String): RIO[SessionRepository, Iterable[Session]]             =
    ZIO.serviceWithZIO[SessionRepository](_.getAllSessionsByActorGuid(guid))
  def getUsersWithRulesTrainedByDay(day: LocalDate): RIO[SessionRepository, Iterable[UserWithRules]] =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersWithRulesTrainedByDay(day))

  def getUsersNameAndFirstName(userId: String): RIO[SessionRepository, UserFirstnameAndLastname] =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersNameAndFirstName(userId))

  def getRulesProgressByUserId(userId: String): RIO[SessionRepository, RulesProgressByUserId]            =
    ZIO.serviceWithZIO[SessionRepository](_.getRulesProgressByUserId(userId))
  def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): RIO[SessionRepository, Iterable[String]] =
    ZIO.serviceWithZIO[SessionRepository](_.getLevelIdsByUserIdByDay(userId, day))
