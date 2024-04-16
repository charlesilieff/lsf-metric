package fr.rebaze.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class UserWithRules(actorGuid: String, rulesIds: Seq[String])

object UserWithRules:
  given interactionZioEncoder: zio.json.JsonEncoder[UserWithRules] = DeriveJsonEncoder.gen[UserWithRules]
  given interactionZioDecoder: zio.json.JsonDecoder[UserWithRules] = DeriveJsonDecoder.gen[UserWithRules]
