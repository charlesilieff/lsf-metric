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

  val sessionLive: ZServerEndpoint[MetricsService, Any] = findOneGuid.serverLogicSuccess { localDate =>
       for

        allUserProgress <- MetricsService.getActorsProgressByDay(localDate)
        _               <- ZIO.logInfo(s"Starting processing ${allUserProgress.size} users !")
        results         <- ZIO
                             .foreachPar(allUserProgress)(session =>
                               for {
                                 _ <- ZIO.logInfo(s"Spark processing session for actor ${session.actorGuid}")

                                 sessionDuration = Spark.getSessionTimeByUserId(session.actorGuid)
                                 _ <- ZIO.logInfo(s"Spark processed session for actor ${session.actorGuid} !")
                               } yield SessionMetric(
                                 userId = session.actorGuid,
                                 trainingDuration = (sessionDuration.averageSessionTime * sessionDuration.sessionCount),
                                 completionPercentage = session.completionPercentage,
                                 lastUseDate = sessionDuration.lastSession,
                                 levelsProgress = session
                                   .levelProgress.map[(String, LevelProgressAndDuration)](pp =>
                                     pp.levelId.toString -> LevelProgressAndDuration(
                                       pp.completionPercentage,
                                       pp.rules.map((ruleId, answer) => (ruleId.toString, answer)))).toMap
                               )).withParallelism(4)
        _               <- ZIO.logInfo(s"Processed ${results.size} sessions !!")
      yield results
    }
  // private def SessionNotFoundMessage(guid: String): String = s"Session with guid ${guid} doesn't exist."
