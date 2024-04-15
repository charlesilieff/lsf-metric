package fr.rebaze.adapters

import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.models.{Interaction, Session as SessionModel}
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.{Task, ZIO, ZLayer}

import java.util.UUID

case class SessionRow(
  guid: UUID,
  actorGuid: String,
  levelGuid: UUID,
  interaction: JsonValue[Interaction]
)

object SessionRepositoryLive:
  val layer: ZLayer[Quill.Postgres[CamelCase], Nothing, SessionRepositoryLive] =
    ZLayer.fromFunction(SessionRepositoryLive(_))
final case class SessionRepositoryLive(quill: Quill.Postgres[CamelCase]) extends SessionRepository:
  import quill.*

  inline private def queryArticle                                                   = quote(
    querySchema[SessionRow](entity = "SessionInteractionsWithAutoincrementId", _.actorGuid -> "actorGuid", _.levelGuid -> "levelGuid"))
  override def getSessionByActorGuid(actorGuid: String): Task[Option[SessionModel]] =
    run(queryArticle.filter(_.actorGuid == lift(actorGuid)).take(1))
      .tap(x => ZIO.logInfo(s"Found $x")).map(value =>
        value.headOption.map(session => new SessionModel("totto", actorGuid = session.actorGuid, "rrrr", session.interaction.value)))
