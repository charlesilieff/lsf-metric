package fr.rebaze.domain.services.metrics

import fr.rebaze.domain.ports.repository.models.LevelId
import fr.rebaze.domain.services.metrics.models.ActorProgress
import zio.*
import zio.nio.file.Path

import java.time.LocalDate

trait MetricsService:
  def getActorsProgressByDay(day: LocalDate): Task[Iterable[ActorProgress]]
  def extractLevelIdsFromJsonDirectExport(path: Path = Path("/src/main/resources/rules/")): Task[Iterable[LevelId]]
  def getGlobalProgressByActorGuid(actorGuid: String, path: Path = Path("/src/main/resources/rules/")): Task[Double]
  def getLevelIdsByActorGuidByDay(actorGuid: String, day: LocalDate): Task[Iterable[String]]
object MetricsService:
  def getActorsProgressByDay(day: LocalDate): RIO[MetricsService, Iterable[ActorProgress]]                                          =
    ZIO
      .serviceWithZIO[MetricsService](_.getActorsProgressByDay(day))
  def extractLevelIdsFromJsonDirectExport(path: Path = Path("/src/main/resources/rules/")): RIO[MetricsService, Iterable[LevelId]]   =
    ZIO.serviceWithZIO[MetricsService](_.extractLevelIdsFromJsonDirectExport(path))
  def getGlobalProgressByActorGuid(actorGuid: String, path: Path = Path("/src/main/resources/rules/")): RIO[MetricsService, Double] =
    ZIO.serviceWithZIO[MetricsService](_.getGlobalProgressByActorGuid(actorGuid, path))

  def getLevelIdsByActorGuidByDay(actorGuid: String, day: LocalDate): RIO[MetricsService, Iterable[String]] =
    ZIO.serviceWithZIO[MetricsService](_.getLevelIdsByActorGuidByDay(actorGuid, day))
