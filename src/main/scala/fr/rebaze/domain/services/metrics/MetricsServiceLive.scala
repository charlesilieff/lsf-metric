package fr.rebaze.domain.services.metrics

import fr.rebaze.domain.ports.engine.Engine
import fr.rebaze.domain.ports.repository.SessionRepository
import fr.rebaze.domain.ports.repository.models.{ActorGuid, LevelId}
import fr.rebaze.domain.services.metrics.errors.Exceptions.NotFound
import fr.rebaze.domain.services.metrics.models.{ActorProgress, Application, LevelProgress}
import zio.json.{DecoderOps, JsonDecoder}
import zio.nio.*
import zio.nio.file.{Files, Path}
import zio.{Ref, Task, ZIO, ZLayer}

import java.net.URI
import java.time.LocalDate

object MetricsServiceLive:
  val layer: ZLayer[SessionRepository & Engine, Nothing, MetricsServiceLive] =
    ZLayer(
      for {
        engine      <- ZIO.service[Engine]
        sessionRepo <- ZIO.service[SessionRepository]
        rulesRef    <- Ref.make[Option[Iterable[LevelId]]](None)
      } yield MetricsServiceLive(sessionRepo, rulesRef, engine))
final case class MetricsServiceLive(
  sessionRepository: SessionRepository,
  rulesRef: Ref[Option[Iterable[LevelId]]],
  engine: Engine
) extends MetricsService:
  override def extractLevelIdsFromJsonDirectExport(path: Path = Path("/src/main/resources/rules/")): Task[Iterable[LevelId]] =
    rulesRef.get.flatMap {
      case Some(rules) => ZIO.succeed(rules)
      case None        =>
        val currentPath = System.getProperty("user.dir")
        val fileNames   = List("json_direct_export_4.json", "json_direct_export_5.json", "json_direct_export_6.json")

        val filePaths = fileNames.map(name => URI.create(s"file:///$currentPath$path/$name"))
        for {
          files    <- ZIO
                        .foreachPar(filePaths)(filePath => Files.readAllBytes(Path(filePath))).mapBoth(
                          e => NotFound(e.getMessage),
                          bytes => bytes.map(byte => new String(byte.toArray, "UTF-8")))
          levelIds <- ZIO
                        .foreachPar(files)(file => ZIO.fromEither(file.fromJson[Application]).mapError(e => NotFound(e))).map(files =>
                          files.map(_.levels)).map(files => files.flatMap(_.map(_.guid)))
          _        <- rulesRef.set(Some(levelIds))
          _        <- ZIO.logInfo(s"LevelIds extracted: ${levelIds.size}")
        } yield levelIds
    }

  override def getActorsProgressByDay(day: LocalDate): Task[Map[ActorGuid, ActorProgress]] =
    for {
      actorGuids                         <- sessionRepository.getActorGuidsByDay(day)
      _                                  <- ZIO.logInfo(s"Starting get users global progress for date ${day} and ${actorGuids.size} actors !")
      actorLevelsProgressAndRulesAnswers <- sessionRepository.getActorsLevelsProgressAndRulesAnswers(actorGuids)
      _                                  <- ZIO.logInfo(s"Users levels progress and rules answers for $day found !")
      _                                  <- ZIO.logInfo(s"Starting to calculate global progress for $day and ${actorLevelsProgressAndRulesAnswers.size} users !")

      metrics <- ZIO.foreach(actorLevelsProgressAndRulesAnswers) { userLevelProgressAndRulesAnswers =>
                   for {

                     actorGlobalProgress       <- getGlobalProgressByActorGuid(userLevelProgressAndRulesAnswers.actorGuid)
                     rulesTrainingIdsWithAnswer =
                       userLevelProgressAndRulesAnswers
                         .levelProgress.flatMap(value => value._2.rulesAnswers).map((ruleId, ruleAnswers) =>
                           (ruleId, engine.isRuleLearned(ruleAnswers)))

                     levelProgress =
                       userLevelProgressAndRulesAnswers
                         .levelProgress.map(levelProgress =>
                           val rules =
                             levelProgress._2.rulesAnswers.map((ruleId, ruleAnswers) => (ruleId, engine.isRuleLearned(ruleAnswers)))
                           (
                             levelProgress._1,
                             LevelProgress(
                               levelId = levelProgress._1,
                               completionPercentage = levelProgress._2.completionPercentage,
                               completionDate = levelProgress._2.completionDate,
                               rules = rules
                             ))
                         )

                   } yield (
                     userLevelProgressAndRulesAnswers.actorGuid,
                     ActorProgress(
                       actorGlobalProgress,
                       levelProgress,
                       knownRulesNbr = rulesTrainingIdsWithAnswer.count(_._2),
                       totalRulesNbr = rulesTrainingIdsWithAnswer.size
                     ))
                 }
      _       <- ZIO.logInfo(s" Global progress for $day and ${actorLevelsProgressAndRulesAnswers.size} users calculated !")
    } yield metrics.toMap

  override def getGlobalProgressByActorGuid(actorGuid: ActorGuid, path: Path = Path("/src/main/resources/rules/")): Task[Double] =
    for {
      _                     <- ZIO.logDebug(s"Starting to calculate global progress for user $actorGuid")
      rulesProgressByUserId <- sessionRepository.getRulesProgressByActorGuid(actorGuid)
      rulesOption           <- rulesRef.get
      rules                 <- ZIO.fromOption(rulesOption).catchAll(_ => extractLevelIdsFromJsonDirectExport(path))
      rulesCount             = rules.size
      progressExistingRules  = rulesProgressByUserId.progress.filter((ruleId, _) => rules.toSeq.contains(ruleId))
      average                = progressExistingRules.values.sum / rulesCount
      _                     <- ZIO.logDebug(s"Global progress for user $actorGuid calculated !")
    } yield average

  // TODO: delete this method
  override def getLevelIdsByActorGuidByDay(actorGuid: ActorGuid, day: LocalDate): Task[Iterable[String]] =
//    sessionRepository.getLevelIdsByUserIdByDay(userId, day)
    ZIO.succeed(List.empty)
