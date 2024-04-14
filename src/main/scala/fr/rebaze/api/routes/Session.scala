package fr.rebaze.api.routes

import fr.rebaze.models.Session
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.ZIO

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

  val sessionLive: ZServerEndpoint[Any, Any] = findOneGuid
      .serverLogicSuccess(guid => 
        val session = new Session("123", guid, "123", None)
        ZIO.logInfo("find all beds endpoint called").as(new Session("123", guid, "123", None)))
