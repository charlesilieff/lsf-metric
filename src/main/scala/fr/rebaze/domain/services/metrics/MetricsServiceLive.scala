package fr.rebaze.domain.services.metrics

import fr.rebaze.domain.ports.engine.Engine
import fr.rebaze.domain.ports.repository.SessionRepository
import fr.rebaze.domain.ports.repository.models.RuleId
import fr.rebaze.domain.services.metrics.errors.Exceptions.NotFound
import fr.rebaze.domain.services.metrics.models.{Application, LevelProgress, UserProgress}
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
final case class MetricsServiceLive(sessionRepository: SessionRepository, rulesRef: Ref[Option[Iterable[RuleId]]], engine: Engine)
    extends MetricsService:
  val extractRulesIdFromJsonDirectExport: Task[Iterable[RuleId]] =
    rulesRef.get.flatMap {
      case Some(rules) => ZIO.succeed(rules)
      case None        =>
        val currentPath = System.getProperty("user.dir")
        val fileNames   = List("json_direct_export_4.json", "json_direct_export_5.json", "json_direct_export_6.json")

        val filePaths = fileNames.map(name => URI.create(s"file:///$currentPath/src/main/resources/rules/$name"))
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

  override def getUsersProgressByDay(day: LocalDate): Task[Iterable[UserProgress]] =
    for {
      userLevelsProgressAndRulesAnswers <- sessionRepository.getUsersLevelsProgressAndRulesAnswers(day)

      metrics <- ZIO.foreach(userLevelsProgressAndRulesAnswers) { userLevelsProgressAndRulesAnswers =>
                   for {
                     globalProgress             <- getGlobalProgressByUserId(userLevelsProgressAndRulesAnswers.actorGuid)
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
                   } yield UserProgress(
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
    } yield metrics.toSeq

  override def getGlobalProgressByUserId(userId: String): Task[Double] =
    for {
      rulesProgressByUserId <- sessionRepository.getRulesProgressByUserId(userId)
      rulesOption           <- rulesRef.get
      rules                 <- ZIO.fromOption(rulesOption).catchAll(_ => extractRulesIdFromJsonDirectExport)
      rulesCount             = rules.size
      progressExistingRules  = rulesProgressByUserId.progress.filter((ruleId, _) => rules.toSeq.contains(ruleId))
      average                = progressExistingRules.values.sum / rulesCount
    } yield average

  // TODO: delete this method
  override def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): Task[Iterable[String]] =
//    sessionRepository.getLevelIdsByUserIdByDay(userId, day)
    ZIO.succeed(List.empty)
