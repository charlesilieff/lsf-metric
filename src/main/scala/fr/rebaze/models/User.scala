package fr.rebaze.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class User(actorGuid: String)

object User:
  given interactionZioEncoder: zio.json.JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  given interactionZioDecoder: zio.json.JsonDecoder[User] = DeriveJsonDecoder.gen[User]
