package fr.rebaze.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class Interaction(ruleId: String, correct: Boolean, progress: Option[Double], timestamp: Long, exerciseId: String)

object Interaction {
  implicit val interactionZioEncoder: zio.json.JsonEncoder[Interaction] = DeriveJsonEncoder.gen[Interaction]
  implicit val interactionZioDecoder: zio.json.JsonDecoder[Interaction] = DeriveJsonDecoder.gen[Interaction]
}
