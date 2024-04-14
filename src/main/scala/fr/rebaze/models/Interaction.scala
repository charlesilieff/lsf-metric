package fr.rebaze.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
import sttp.tapir.generic.auto.*

import java.util.UUID

case class Interaction(ruleId: String, correct: Boolean, progress: Float, timestamp: String, exerciseId: UUID)

object Interaction:

  given interactionZioEncoder: zio.json.JsonEncoder[Interaction] = DeriveJsonEncoder.gen[Interaction]
  given interactionZioDecoder: zio.json.JsonDecoder[Interaction] = DeriveJsonDecoder.gen[Interaction]