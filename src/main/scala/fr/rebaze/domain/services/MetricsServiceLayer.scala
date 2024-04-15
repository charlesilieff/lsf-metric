package fr.rebaze.domain.services

import fr.rebaze.common.Exceptions.NotFound
import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.domain.services.models.Metric
import fr.rebaze.domain.services.models.UserTenant.*
import fr.rebaze.models.UserFirstnameAndLastname
import zio.json.{DecoderOps, DeriveJsonCodec, DeriveJsonDecoder, JsonCodec, JsonDecoder}
import zio.nio.*
import zio.nio.file.{Files, Path}
import zio.{Task, ZIO, ZLayer}

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

object MetricsServiceLayer:
  val layer: ZLayer[SessionRepository, Nothing, MetricsServiceLayer] =
    ZLayer.fromFunction(MetricsServiceLayer(_))
final case class MetricsServiceLayer(sessionRepository: SessionRepository) extends MetricsService:
  override val extractRulesIdFromJsonDirectExport: Task[Seq[String]] =
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
    } yield rules

  override def getMetricsByDay(day: LocalDate): Task[Seq[Metric]] =
    for {
      userIds <- sessionRepository.getUsersByDay(day)
      metrics <- ZIO.foreachPar(userIds) { userId =>
                   for {
                     (nameAndFirstName, userTenant) <-
                       if userId.actorGuid.contains("@voltaire") then ZIO.succeed((UserFirstnameAndLastname(None, None), Voltaire))
                       else sessionRepository.getUsersNameAndFirstName(userId.actorGuid).map((_, Lsf))
                   } yield Metric(
                     userId = userId.actorGuid,
                     lastname = nameAndFirstName.lastname,
                     firstname = nameAndFirstName.firstname,
                     userTenant)
                 }
    } yield metrics

  override def getGlobalProgressByUserId(userId: String): Task[Float] =
    extractRulesIdFromJsonDirectExport.map(rules => rules).as(0.23f)
