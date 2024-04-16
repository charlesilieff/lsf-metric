package fr.rebaze.api.routes

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class SessionMetric(
  userId: String,
  userTenant: String,
  firstName: Option[String],
  lastName: Option[String],
  trainingDuration: Long,
  completionPercentage: Double,
  lastUseDate: Long,
  levelProgress: List[LevelProgress])

object SessionMetric:
  given sessionZioEncoder: zio.json.JsonEncoder[SessionMetric] = DeriveJsonEncoder.gen[SessionMetric]
  given sessionZioDecoder: zio.json.JsonDecoder[SessionMetric] = DeriveJsonDecoder.gen[SessionMetric]
case class LevelProgress(levelId: String, completionPercentage: Int)

object LevelProgress:
  given sessionZioEncoder: zio.json.JsonEncoder[LevelProgress] = DeriveJsonEncoder.gen[LevelProgress]
  given sessionZioDecoder: zio.json.JsonDecoder[LevelProgress] = DeriveJsonDecoder.gen[LevelProgress]
