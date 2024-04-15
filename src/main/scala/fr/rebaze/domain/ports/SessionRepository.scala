package fr.rebaze.domain.ports

import fr.rebaze.models.{Session, User, UserFirstnameAndLastname}
import zio.*

import java.time.LocalDate

trait SessionRepository:
  def getSessionByActorGuid(guid: String): Task[Option[Session]]
  def getUsersByDay(day: LocalDate): Task[Seq[User]]
  def getUsersNameAndFirstName(userId: String): Task[UserFirstnameAndLastname]
object SessionRepository:
  def getSessionByActorGuid(guid: String): RIO[SessionRepository, Option[Session]] =
    ZIO.serviceWithZIO[SessionRepository](_.getSessionByActorGuid(guid))
  def getUsersByDay(day: LocalDate): RIO[SessionRepository, Seq[User]]             =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersByDay(day))

  def getUsersNameAndFirstName(userId: String): RIO[SessionRepository, UserFirstnameAndLastname] =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersNameAndFirstName(userId))  
