package fr.rebaze.adapters

import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.models.Session as SessionModel
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.{Task, ZIO, ZLayer}

object SessionRepositoryLive:
  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, SessionRepositoryLive] =
    ZLayer.fromFunction(SessionRepositoryLive(_))
final case class SessionRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends SessionRepository:
  import quill.*

  override def getSessionByGuid(guid: String): Task[SessionModel] =
    ZIO.succeed(new SessionModel("totto", "rrrr", "rrrr", None))
