package fr.rebaze.domain.services

import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.domain.services.models.Metric
import fr.rebaze.models.UserFirstnameAndLastname
import zio.{Task, ZIO, ZLayer}

import java.time.LocalDate

object MetricsServiceLayer:
  val layer: ZLayer[SessionRepository, Nothing, MetricsServiceLayer] =
    ZLayer.fromFunction(MetricsServiceLayer(_))
final case class MetricsServiceLayer(sessionRepository: SessionRepository) extends MetricsService:
  override def getMetricsByDay(day: LocalDate): Task[Seq[Metric]] =
    for {
      userIds <- sessionRepository.getUsersByDay(day)
      metrics <- ZIO.foreachPar(userIds) { userId =>
                   for {
                     nameAndFirstName <-
                       if userId.actorGuid.contains("@voltaire") then ZIO.succeed(UserFirstnameAndLastname( None, None))
                       else sessionRepository.getUsersNameAndFirstName(userId.actorGuid)
                   } yield Metric(userId = userId.actorGuid, lastname = nameAndFirstName.lastname, firstname = nameAndFirstName.firstname)
                 }
    } yield metrics
