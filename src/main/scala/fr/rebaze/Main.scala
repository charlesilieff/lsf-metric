package fr.rebaze

import fr.rebaze.api.routes.Endpoints
import fr.rebaze.common.Layer
import fr.rebaze.domain.services.metrics.MetricsService
import sttp.tapir.server.interceptor.cors.CORSConfig.AllowedOrigin
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.http.*
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault:
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = SLF4J.slf4j(LogFormat.colored)
  given RIOMonadError[Any]                             = new RIOMonadError[Any]

  override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] =

    val port = sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(8080)

    val options: ZioHttpServerOptions[Any] = ZioHttpServerOptions
      .customiseInterceptors
      .corsInterceptor(
        CORSInterceptor.customOrThrow(
          CORSConfig
            .default.copy(
              allowedOrigin = AllowedOrigin.All
            )
        )
      )
      .options
    val app: HttpApp[Any]                  = ZioHttpInterpreter(options).toHttp(Endpoints.all)
    val sessionsApp                        = ZioHttpInterpreter(options).toHttp(Endpoints.sessionEndpoint)
    val all: HttpApp[MetricsService]       = app ++ sessionsApp

    (for
      _          <- Console.printLine(s"Starting server on port $port")
      actualPort <- Server.install(all) // or .serve if you don't need the port and want to keep it running without manual readLine
      _          <- Console.printLine(s"Go to http://localhost:${actualPort}/docs to open")
      _          <- ZIO.never
    yield ())
      .provide(
        Server.defaultWithPort(port),
        Layer.prodLayer
      )
