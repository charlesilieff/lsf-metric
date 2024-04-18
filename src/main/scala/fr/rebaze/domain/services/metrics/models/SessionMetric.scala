package fr.rebaze.domain.services.metrics.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class SessionMetric(
  userId: String,
  trainingDuration: Long,
  completionPercentage: Double,
  lastUseDate: Long,
  levelsProgress: Map[String, LevelProgressAndDuration])

case class LevelProgressAndDuration(completionPercentage: Double)

object LevelProgressAndDuration:
  given sessionZioEncoder: zio.json.JsonEncoder[LevelProgressAndDuration] = DeriveJsonEncoder.gen[LevelProgressAndDuration]

  given sessionZioDecoder: zio.json.JsonDecoder[LevelProgressAndDuration] = DeriveJsonDecoder.gen[LevelProgressAndDuration]
object SessionMetric:
  given sessionZioEncoder: zio.json.JsonEncoder[SessionMetric] = DeriveJsonEncoder.gen[SessionMetric]
  given sessionZioDecoder: zio.json.JsonDecoder[SessionMetric] = DeriveJsonDecoder.gen[SessionMetric]
