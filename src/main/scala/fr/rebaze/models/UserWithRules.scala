package fr.rebaze.models

import fr.rebaze.adapters.LevelProgress
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
import zio.prelude.Newtype

case class UserWithRules(actorGuid: String, levelProgress: Iterable[LevelProgress])

object UserWithRules:
  given interactionZioEncoder: zio.json.JsonEncoder[UserWithRules] = DeriveJsonEncoder.gen[UserWithRules]
  given interactionZioDecoder: zio.json.JsonDecoder[UserWithRules] = DeriveJsonDecoder.gen[UserWithRules]
