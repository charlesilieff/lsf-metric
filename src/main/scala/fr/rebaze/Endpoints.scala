package fr.rebaze

import fr.rebaze.api.routes.Session.sessionLive
import fr.rebaze.domain.ports.SessionRepository
import sttp.tapir.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.{Task, ZIO}

object Endpoints:
  case class User(name: String) extends AnyVal
  val helloEndpoint: PublicEndpoint[User, Unit, String, Any] = endpoint
    .get
    .in("hello")
    .in(query[User]("name"))
    .out(stringBody)
  val helloServerEndpoint: ZServerEndpoint[Any, Any]         =
    helloEndpoint.serverLogicSuccess(user => ZIO.logInfo("Hello there").as(s"Hello ${user.name}"))

  val apiEndpoints: List[ZServerEndpoint[Any, Any]] = List(helloServerEndpoint)

  val sessionEndpoint: ZServerEndpoint[SessionRepository, Any] = sessionLive

  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "lsf-metrics", "1.0.0")

  val all: List[ZServerEndpoint[Any, Any]] = apiEndpoints ++ docEndpoints
