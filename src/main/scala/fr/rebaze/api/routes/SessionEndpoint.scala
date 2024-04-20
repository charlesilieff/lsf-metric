package fr.rebaze.api.routes

import fr.rebaze.domain.ports.spark.Spark
import fr.rebaze.domain.services.metrics.MetricsService
import fr.rebaze.domain.services.metrics.models.{LevelProgressAndDuration, SessionMetric}
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.ZIO

import java.time.LocalDate

object SessionEndpoint:
  private val findOneGuid: Endpoint[Unit, LocalDate, ErrorInfo, Iterable[SessionMetric], Any] =
    endpoint
      .name("findOneGuid")
      .get
      .in("api" / "session" / path[LocalDate]("localDate"))
      .errorOut(
        oneOf(
          oneOfVariant(statusCode(StatusCode.Unauthorized) and jsonBody[ErrorInfo])
        )
      )
      .out(jsonBody[Iterable[SessionMetric]])

  val sessionLive: ZServerEndpoint[MetricsService, Any] = findOneGuid
    .serverLogicSuccess { localDate =>
      for {
        allUserProgress <- MetricsService.getUsersProgressByDay(localDate)
        sessionDuration  = Spark.getSessionTimeByUserId(allUserProgress.map(_.actorGuid))
        _               <- ZIO.logInfo(s"Processing ${allUserProgress.size} lsf user sessions")
        results         <- ZIO
                             .foreachPar(sessionDuration)(userProgress =>
                               for {
                                 _ <- ZIO.logInfo(s"Processing session for actor ${userProgress.userId}")

                               } yield SessionMetric(
                                 userId = userProgress.userId,
                                 trainingDuration = (userProgress.averageSessionTime * userProgress.sessionCount).toMillis,
                                 completionPercentage = 0.5,
                                 lastUseDate = userProgress.lastSession,
                                 levelsProgress = Map.empty
                               )).withParallelism(4)
        _               <- ZIO.logInfo(s"Processed ${results.size} sessions !!")

      } yield results

    }
  // private def SessionNotFoundMessage(guid: String): String = s"Session with guid ${guid} doesn't exist."
