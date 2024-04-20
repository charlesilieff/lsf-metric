package fr.rebaze.domain.ports.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
import fr.rebaze.domain.ports.repository.models.RuleId
case class RulesProgressByUserId(userId: String, progress: Map[RuleId, Double])

object RulesProgressByUserId:
  given interactionZioEncoder: zio.json.JsonEncoder[RulesProgressByUserId] = DeriveJsonEncoder.gen[RulesProgressByUserId]
  given interactionZioDecoder: zio.json.JsonDecoder[RulesProgressByUserId] = DeriveJsonDecoder.gen[RulesProgressByUserId]
