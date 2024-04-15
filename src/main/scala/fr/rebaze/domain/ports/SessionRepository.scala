package fr.rebaze.domain.ports

import fr.rebaze.models.{Session, User, UserFirstnameAndLastname}
import zio.*

import java.time.LocalDate

trait SessionRepository:
  def getAllSessionsByActorGuid(guid: String): Task[Seq[Session]]
  def getUsersByDay(day: LocalDate): Task[Seq[User]]
  def getUsersNameAndFirstName(userId: String): Task[UserFirstnameAndLastname]
object SessionRepository:
  def getAllSessionsByActorGuid(guid: String): RIO[SessionRepository, Seq[Session]] =
    ZIO.serviceWithZIO[SessionRepository](_.getAllSessionsByActorGuid(guid))
  def getUsersByDay(day: LocalDate): RIO[SessionRepository, Seq[User]]             =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersByDay(day))

  def getUsersNameAndFirstName(userId: String): RIO[SessionRepository, UserFirstnameAndLastname] =
    ZIO.serviceWithZIO[SessionRepository](_.getUsersNameAndFirstName(userId))  
