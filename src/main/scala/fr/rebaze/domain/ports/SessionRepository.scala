package fr.rebaze.domain.ports

import fr.rebaze.models.Session

import zio.*

trait SessionRepository:
  def getSessionByActorGuid(guid: String): Task[Option[Session]]
object SessionRepository:
  def getSessionByActorGuid(guid: String): RIO[SessionRepository, Option[Session]] =
    ZIO.serviceWithZIO[SessionRepository](_.getSessionByActorGuid(guid))
