package fr.rebaze.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class Interaction(ruleId: String, correct: Boolean, progress: Option[Float], timestamp: Long, exerciseId: String)

object Interaction:
  given interactionZioEncoder: zio.json.JsonEncoder[Interaction] = DeriveJsonEncoder.gen[Interaction]
  given interactionZioDecoder: zio.json.JsonDecoder[Interaction] = DeriveJsonDecoder.gen[Interaction]
