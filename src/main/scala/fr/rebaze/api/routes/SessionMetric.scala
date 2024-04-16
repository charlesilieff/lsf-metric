package fr.rebaze.api.routes

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class SessionMetric(
  userId: String,
  userTenant: String,
  firstName: Option[String],
  lastName: Option[String],
  trainingDuration: Long,
  completionPercentage: Double,
  lastUseDate: Long)

object SessionMetric:
  given sessionZioEncoder: zio.json.JsonEncoder[SessionMetric] = DeriveJsonEncoder.gen[SessionMetric]
  given sessionZioDecoder: zio.json.JsonDecoder[SessionMetric] = DeriveJsonDecoder.gen[SessionMetric]
