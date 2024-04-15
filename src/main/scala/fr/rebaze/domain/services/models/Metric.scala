package fr.rebaze.domain.services.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class Metric(userId:String, lastname: Option[String], firstname: Option[String])

object Metric:
  given interactionZioEncoder: zio.json.JsonEncoder[Metric] = DeriveJsonEncoder.gen[Metric]
  given interactionZioDecoder: zio.json.JsonDecoder[Metric] = DeriveJsonDecoder.gen[Metric]
