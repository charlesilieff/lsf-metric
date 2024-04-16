package fr.rebaze.api.routes

import fr.rebaze.common.Exceptions.NotFound
import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.models.Session
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*

object Session:
  private val findOneGuid: Endpoint[Unit, String, ErrorInfo, Session, Any] =
    endpoint
      .name("findOneGuid")
      .get
      .in("api" / "session" / path[String]("guid"))
      .errorOut(
        oneOf(
          oneOfVariant(statusCode(StatusCode.Unauthorized) and jsonBody[ErrorInfo])
        )
      )
      .out(jsonBody[Session])

  val sessionLive: ZServerEndpoint[SessionRepository, Any] = findOneGuid
    .serverLogicSuccess(guid => SessionRepository.getAllSessionsByActorGuid(guid).map(sessions=>sessions.headOption).someOrFail(NotFound(SessionNotFoundMessage(guid))))
  
  private def SessionNotFoundMessage(guid: String): String = s"Session with guid ${guid} doesn't exist."