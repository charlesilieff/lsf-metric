package fr.rebaze.api.routes

import fr.rebaze.api.routes.SessionEndpoint.sessionLive
import sttp.tapir.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.{Task, ZIO}

object Endpoints:
  case class User(name: String) extends AnyVal
  val helloEndpoint: PublicEndpoint[Unit, Unit, String, Any] = endpoint
    .get
    .in("health")
    .out(stringBody)
  val helloServerEndpoint: ZServerEndpoint[Any, Any]         =
    helloEndpoint.serverLogicSuccess(user => ZIO.logInfo("Hello there !").as(s"Hello there !"))

  val apiEndpoints: List[ZServerEndpoint[Any, Any]] = List(helloServerEndpoint)

  val sessionEndpoint = sessionLive

  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "lsf-metrics", "1.0.0")

  val all: List[ZServerEndpoint[Any, Any]] = apiEndpoints ++ docEndpoints
