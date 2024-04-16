package fr.rebaze.api.routes

import fr.rebaze.adapters.LevelId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
import zio.prelude.Newtype

case class SessionMetric(
  userId: String,
  userTenant: String,
  firstName: Option[String],
  lastName: Option[String],
  trainingDuration: Long,
  completionPercentage: Double,
  lastUseDate: Long,
  levelProgress: Map[String, LevelProgressAndDuration])

case class LevelProgressAndDuration(completionPercentage: Double)

object LevelProgressAndDuration:
  given sessionZioEncoder: zio.json.JsonEncoder[LevelProgressAndDuration] = DeriveJsonEncoder.gen[LevelProgressAndDuration]

  given sessionZioDecoder: zio.json.JsonDecoder[LevelProgressAndDuration] = DeriveJsonDecoder.gen[LevelProgressAndDuration]
object SessionMetric:
  given sessionZioEncoder: zio.json.JsonEncoder[SessionMetric] = DeriveJsonEncoder.gen[SessionMetric]
  given sessionZioDecoder: zio.json.JsonDecoder[SessionMetric] = DeriveJsonDecoder.gen[SessionMetric]
