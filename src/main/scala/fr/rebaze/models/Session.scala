package fr.rebaze.models

import zio.json.{DeriveJsonCodec, DeriveJsonDecoder, DeriveJsonEncoder, JsonCodec}

case class Session(guid: String, actorGuid: String, levelGuid: String, interaction: Interaction)

object Session {
  implicit val sessionZioEncoder: zio.json.JsonEncoder[Session] = DeriveJsonEncoder.gen[Session]
  implicit val sessionZioDecoder: zio.json.JsonDecoder[Session] = DeriveJsonDecoder.gen[Session]

  implicit val codec: JsonCodec[Session] =
    DeriveJsonCodec.gen[Session]
}
