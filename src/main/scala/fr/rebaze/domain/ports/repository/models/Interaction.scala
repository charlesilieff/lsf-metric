package fr.rebaze.domain.ports.repository.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class Interaction(ruleId: RuleId, correct: Boolean, progress: Option[Double], timestamp: Long, exerciseId: String)

object Interaction:
  given interactionZioEncoder: zio.json.JsonEncoder[Interaction] = DeriveJsonEncoder.gen[Interaction]
  given interactionZioDecoder: zio.json.JsonDecoder[Interaction] = DeriveJsonDecoder.gen[Interaction]
