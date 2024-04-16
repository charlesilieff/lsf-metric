package fr.rebaze.api.routes

import fr.rebaze.adapters.LevelProgress
import fr.rebaze.domain.services.MetricsService
import fr.rebaze.domain.services.spark.Spark
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.ZIO

import java.time.LocalDate

object Session:
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
        _               <- ZIO.logInfo(s"Processing ${allUserProgress.size} user sessions")
        results         <- ZIO
                             .foreachPar(allUserProgress)(session =>
                               for {
                                 _ <- ZIO.logInfo(s"Processing session for actor ${session.actorGuid}")

                                 sessionDuration = Spark.getSessionTimeByUserId(session.actorGuid)
                               } yield SessionMetric(
                                 userId = session.actorGuid,
                                 userTenant = session.userTenant.toString,
                                 firstName = session.firstname,
                                 lastName = session.lastname,
                                 trainingDuration = (sessionDuration.averageSessionTime * sessionDuration.sessionCount).toMillis,
                                 completionPercentage = session.completionPercentage,
                                 lastUseDate = sessionDuration.lastSession,
                                 levelProgress = session
                                   .levelProgress.map[(String, LevelProgressAndDuration)](pp =>
                                     pp.levelId.toString -> LevelProgressAndDuration(pp.completionPercentage)).toMap
                               )).withParallelism(4)
        _               <- ZIO.logInfo(s"Processed ${results.size} sessions !!")

      } yield results

    }
  // private def SessionNotFoundMessage(guid: String): String = s"Session with guid ${guid} doesn't exist."
