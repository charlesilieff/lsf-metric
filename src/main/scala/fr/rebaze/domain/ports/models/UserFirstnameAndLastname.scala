package fr.rebaze.models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class UserFirstnameAndLastname(lastname: Option[String], firstname: Option[String])

object UserFirstnameAndLastname:
  given interactionZioEncoder: zio.json.JsonEncoder[UserFirstnameAndLastname] = DeriveJsonEncoder.gen[UserFirstnameAndLastname]
  given interactionZioDecoder: zio.json.JsonDecoder[UserFirstnameAndLastname] = DeriveJsonDecoder.gen[UserFirstnameAndLastname]
