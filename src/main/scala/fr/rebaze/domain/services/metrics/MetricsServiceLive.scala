package fr.rebaze.domain.services.metrics


import fr.rebaze.domain.ports.repository.SessionRepository
import fr.rebaze.domain.services.MetricsService
import fr.rebaze.domain.services.metrics.errors.Exceptions.NotFound
import fr.rebaze.domain.services.metrics.models.{LevelProgress, UserProgress}
import zio.json.{DecoderOps, DeriveJsonCodec, DeriveJsonDecoder, JsonCodec, JsonDecoder}
import zio.nio.*
import zio.nio.file.{Files, Path}
import zio.{Ref, Task, ZIO, ZLayer}

import java.net.URI
import java.time.LocalDate

case class Rule(guid: String)

case class Level(rules: List[Rule])

case class Application(levels: List[Level])

object Application {
  given JsonCodec[Rule]        =
    DeriveJsonCodec.gen[Rule]
  given JsonCodec[Level]       =
    DeriveJsonCodec.gen[Level]
  given JsonCodec[Application] =
    DeriveJsonCodec.gen[Application]
}

object MetricsServiceLive:
  val layer: ZLayer[SessionRepository, Nothing, MetricsServiceLive] =
    ZLayer(
      for {
        sessionRepo <- ZIO.service[SessionRepository]
        rulesRef    <- Ref.make[Option[Iterable[String]]](None)
      } yield MetricsServiceLive(sessionRepo, rulesRef))
final case class MetricsServiceLive(sessionRepository: SessionRepository, rulesRef: Ref[Option[Iterable[String]]]) extends MetricsService:
  val extractRulesIdFromJsonDirectExport: Task[Iterable[String]] =
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
                     globalProgress            <- getGlobalProgressByUserId(userLevelsProgressAndRulesAnswers.actorGuid)
                     rulesTrainingIdsWithAnswer = userLevelsProgressAndRulesAnswers.levelProgress.map(value => value._2).toList
                   } yield UserProgress(
                     actorGuid = userLevelsProgressAndRulesAnswers.actorGuid,
                     globalProgress,
                     levelProgress = userLevelsProgressAndRulesAnswers.levelProgress.map(levelProgress =>
                       LevelProgress(
                         levelId = levelProgress._1,
                         completionPercentage = levelProgress.completionPercentage
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
