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

      _                      <- ZIO.logInfo(s"Starting processing ${localDate} users !")
      allUserProgressMap     <- MetricsService.getActorsProgressByDay(localDate)
      _                      <- ZIO.logInfo(s"Processed progress for ${allUserProgressMap.size} users !")
      _                      <- ZIO.logInfo(s"Spark processing session for actors ${allUserProgressMap.size}")
      allActorsSessionTimeMap = Spark.getSessionTimeByActorGuids(allUserProgressMap.keySet)
      _                      <- ZIO.logInfo(s"Spark processed session for actor ${allUserProgressMap.size} !")
      results                 =
        allActorsSessionTimeMap
          .keySet.map(actorGuid =>
            val actorProgress    = allUserProgressMap(actorGuid)
            val actorSession     = allActorsSessionTimeMap(actorGuid)
            val trainingDuration = actorSession.averageSessionTime * actorSession.sessionCount
            SessionMetric(
              userId = actorGuid,
              trainingDuration = trainingDuration,
              completionPercentage = actorProgress.completionPercentage,
              lastUseDate = actorSession.lastSession,
              levelsProgress = actorProgress
                .levelProgress.map[(String, LevelProgressAndDuration)](pp =>
                  pp.levelId.toString -> LevelProgressAndDuration(
                    pp.completionPercentage,
                    pp.rules.map((ruleId, answer) => (ruleId.toString, answer)),
                    pp.completionDate
                  )).toMap,
              totalRulesNbr = actorProgress.totalRulesNbr,
              knownRulesNbr = actorProgress.knownRulesNbr
            )
          )
      _                      <- ZIO.logInfo(s"Processed ${results.size} sessions !!")
    yield results.take(3)
  }
  // private def SessionNotFoundMessage(guid: String): String = s"Session with guid ${guid} doesn't exist."
