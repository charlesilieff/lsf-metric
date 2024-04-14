package fr.rebaze.domain.ports

import fr.rebaze.models.Session

import zio.*

trait SessionRepository:
  def getSessionByGuid(guid: String): Task[Session]
  
object SessionRepository:
  def getSessionByGuid(guid: String): RIO[SessionRepository, Session]   =
    ZIO.serviceWithZIO[SessionRepository](_.getSessionByGuid(guid))
