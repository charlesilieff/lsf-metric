package fr.rebaze.api.routes

import fr.rebaze.common.Exceptions.NotFound
import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.models.Session
import sttp.model.StatusCode
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.ZServerEndpoint
import sttp.tapir._

object Session {
  private val findOneGuid: Endpoint[Unit, String, ErrorInfo, Session, Any] =
    endpoint
      .name("findOneGuid")
      .get
      .in("session" / path[String]("guid"))
      .errorOut(
        oneOf(
          oneOfVariant(statusCode(StatusCode.Unauthorized) and jsonBody[ErrorInfo])
        )
      )
      .out(jsonBody[Session])

  val sessionLive: ZServerEndpoint[SessionRepository, Any] = findOneGuid
    .serverLogicSuccess(guid =>
      SessionRepository
        .getAllSessionsByActorGuid(guid).map(sessions => sessions.headOption).someOrFail(NotFound(SessionNotFoundMessage(guid))))

  private def SessionNotFoundMessage(guid: String): String = s"Session with guid ${guid} doesn't exist."
}
