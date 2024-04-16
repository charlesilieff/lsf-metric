package fr.rebaze

import fr.rebaze.domain.ports.SessionRepository
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio._
import zio.http._
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = SLF4J.slf4j(LogFormat.colored)
  implicit val toto: RIOMonadError[Any]                = new RIOMonadError[Any]

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {

    val port                            = sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(8080)
//    val options = ZioHttpServerOptions
//      .customiseInterceptors
//      .corsInterceptor(
//        CORSInterceptor.customOrThrow(
//          CORSConfig
//            .default.copy(
//              allowedOrigin = AllowedOrigin.All
//            )
//        )
//      )
//      .options
    val app: HttpApp[Any]               = ZioHttpInterpreter().toHttp(Endpoints.all)
    val sessionsApp                     = ZioHttpInterpreter().toHttp(Endpoints.sessionEndpoint)
    val all: HttpApp[SessionRepository] = app ++ sessionsApp

    (for {
      actualPort <- Server.install(all) // or .serve if you don't need the port and want to keep it running without manual readLine
      _          <- Console.printLine(
                      s"Go to http://localhost:${actualPort}/docs to open" +
                        s"n SwaggerUI. Press ENTER key to exit.")
      _          <- ZIO.never
    } yield ())
      .provide(
        Server.defaultWithPort(port),
        Layer.prodLayer
      )
      .exitCode
  }
}
