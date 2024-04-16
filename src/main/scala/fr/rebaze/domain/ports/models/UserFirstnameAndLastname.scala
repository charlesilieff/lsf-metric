package fr.rebaze.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class UserFirstnameAndLastname(lastname: Option[String], firstname: Option[String])

object UserFirstnameAndLastname {
  implicit val interactionZioEncoder: zio.json.JsonEncoder[UserFirstnameAndLastname] = DeriveJsonEncoder.gen[UserFirstnameAndLastname]
  implicit val interactionZioDecoder: zio.json.JsonDecoder[UserFirstnameAndLastname] = DeriveJsonDecoder.gen[UserFirstnameAndLastname]
}
