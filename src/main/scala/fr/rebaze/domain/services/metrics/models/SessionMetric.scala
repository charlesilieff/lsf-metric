package fr.rebaze.domain.services.metrics.models

import fr.rebaze.domain.ports.repository.models.ActorGuid
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class SessionMetric(
  userId: ActorGuid,
  trainingDuration: Long,
  completionPercentage: Double,
  lastUseDate: Long,
  knownRulesNbr: Int,
  totalRulesNbr: Int,
  levelsProgress: Map[String, LevelProgressAndDuration])

case class LevelProgressAndDuration(
  completionPercentage: Double,
  rules: Map[String, Boolean],
  completionDate: Option[Long]
)

object LevelProgressAndDuration:
  given sessionZioEncoder: zio.json.JsonEncoder[LevelProgressAndDuration] = DeriveJsonEncoder.gen[LevelProgressAndDuration]

  given sessionZioDecoder: zio.json.JsonDecoder[LevelProgressAndDuration] = DeriveJsonDecoder.gen[LevelProgressAndDuration]
object SessionMetric:
  given sessionZioEncoder: zio.json.JsonEncoder[SessionMetric] = DeriveJsonEncoder.gen[SessionMetric]
  given sessionZioDecoder: zio.json.JsonDecoder[SessionMetric] = DeriveJsonDecoder.gen[SessionMetric]
