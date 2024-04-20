package fr.rebaze.domain.services.metrics

import fr.rebaze.domain.ports.engine.Engine
import fr.rebaze.domain.ports.repository.SessionRepository
import fr.rebaze.domain.ports.repository.models.RuleId
import fr.rebaze.domain.services.metrics.errors.Exceptions.NotFound
import fr.rebaze.domain.services.metrics.models.{ActorProgress, Application, LevelProgress}
import zio.json.{DecoderOps, JsonDecoder}
import zio.nio.*
import zio.nio.file.{Files, Path}
import zio.{Ref, Task, ZIO, ZLayer}

import java.net.URI
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

object MetricsServiceLive:
  val layer: ZLayer[SessionRepository & Engine, Nothing, MetricsServiceLive] =
    ZLayer(
      for {
        engine      <- ZIO.service[Engine]
        sessionRepo <- ZIO.service[SessionRepository]
        rulesRef    <- Ref.make[Option[Iterable[RuleId]]](None)
      } yield MetricsServiceLive(sessionRepo, rulesRef, engine))
final case class MetricsServiceLive(
  sessionRepository: SessionRepository,
  rulesRef: Ref[Option[Iterable[RuleId]]],
  engine: Engine
) extends MetricsService:
  override def extractRulesIdFromJsonDirectExport(path: Path = Path("/src/main/resources/rules/")): Task[Iterable[RuleId]] =
    rulesRef.get.flatMap {
      case Some(rules) => ZIO.succeed(rules)
      case None        =>
        val currentPath = System.getProperty("user.dir")
        val fileNames   = List("json_direct_export_4.json", "json_direct_export_5.json", "json_direct_export_6.json")

        val filePaths = fileNames.map(name => URI.create(s"file:///$currentPath$path/$name"))
        for {
          files <- ZIO
                     .foreachPar(filePaths)(filePath => Files.readAllBytes(Path(filePath))).mapBoth(
                       e => NotFound(e.getMessage),
                       bytes => bytes.map(byte => new String(byte.toArray, "UTF-8")))
          rules <- ZIO
                     .foreachPar(files)(file => ZIO.fromEither(file.fromJson[Application]).mapError(e => NotFound(e))).map(files =>
                       files.map(_.levels)).map(files => files.map(_.flatMap(_.rules)).flatMap(_.map(_.guid)))
          _     <- rulesRef.set(Some(rules))
          _     <- ZIO.logInfo(s"Rules extracted: ${rules.size}")
        } yield rules
    }

  override def getActorsProgressByDay(day: LocalDate): Task[Iterable[ActorProgress]] =
    for {
      _                                 <- ZIO.logInfo(s"Starting get users global progress for date ${day}")
      userLevelsProgressAndRulesAnswers <- sessionRepository.getUsersLevelsProgressAndRulesAnswers(day)
      _                                 <- ZIO.logInfo(s"Users levels progress and rules answers for $day found !")
      _                                 <- ZIO.logInfo(s"Starting to calculate global progress for $day and ${userLevelsProgressAndRulesAnswers.size} users !")

      metrics <- ZIO.foreach(userLevelsProgressAndRulesAnswers) { userLevelsProgressAndRulesAnswers =>
                   for {
                     globalProgress             <- getGlobalProgressByActorGuid(userLevelsProgressAndRulesAnswers.actorGuid)
                     rulesTrainingIdsWithAnswer <-
                       ZIO.foreach(
                         userLevelsProgressAndRulesAnswers
                           .levelProgress.flatMap(value =>
                             value
                               .rulesAnswers.map((ruleId, ruleAnswers) =>
                                 (
                                   ruleId,
                                   engine.isRuleLearned(ruleAnswers.map((time, answer) =>
                                     (LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()), answer)))))))(
                         (toto, tata) => tata.map(tyty => (toto, tyty)))
                   } yield ActorProgress(
                     actorGuid = userLevelsProgressAndRulesAnswers.actorGuid,
                     globalProgress,
                     levelProgress = userLevelsProgressAndRulesAnswers
                       .levelProgress.map(levelProgress =>
                         LevelProgress(
                           levelId = levelProgress._1,
                           completionPercentage = levelProgress.completionPercentage,
                           rulesTrainingIdsWithAnswer.toMap
                         ))
                   )
                 }
      _       <- ZIO.logInfo(s" Global progress for $day and ${userLevelsProgressAndRulesAnswers.size} users calculated !")
    } yield metrics.toSeq

  override def getGlobalProgressByActorGuid(actorGuid: String, path: Path = Path("/src/main/resources/rules/")): Task[Double] =
    for {
      _                     <- ZIO.logDebug(s"Starting to calculate global progress for user $actorGuid")
      rulesProgressByUserId <- sessionRepository.getRulesProgressByActorGuid(actorGuid)
      rulesOption           <- rulesRef.get
      rules                 <- ZIO.fromOption(rulesOption).catchAll(_ => extractRulesIdFromJsonDirectExport(path))
      rulesCount             = rules.size
      progressExistingRules  = rulesProgressByUserId.progress.filter((ruleId, _) => rules.toSeq.contains(ruleId))
      average                = progressExistingRules.values.sum / rulesCount
      _                     <- ZIO.logDebug(s"Global progress for user $actorGuid calculated !")
    } yield average

  // TODO: delete this method
  override def getLevelIdsByActorGuidByDay(actorGuid: String, day: LocalDate): Task[Iterable[String]] =
//    sessionRepository.getLevelIdsByUserIdByDay(userId, day)
    ZIO.succeed(List.empty)
