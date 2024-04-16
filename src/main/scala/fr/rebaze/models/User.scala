package fr.rebaze.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class User(actorGuid: String)

object User {
  implicit val interactionZioEncoder: zio.json.JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  implicit val interactionZioDecoder: zio.json.JsonDecoder[User] = DeriveJsonDecoder.gen[User]
}
