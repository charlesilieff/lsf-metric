package fr.rebaze.domain.ports.repository.models

import zio.json.{DeriveJsonCodec, DeriveJsonDecoder, DeriveJsonEncoder, JsonCodec}

case class Session(guid: String, actorGuid: String, levelGuid: String, interaction: Interaction)

object Session:
  given sessionZioEncoder: zio.json.JsonEncoder[Session] = DeriveJsonEncoder.gen[Session]
  given sessionZioDecoder: zio.json.JsonDecoder[Session] = DeriveJsonDecoder.gen[Session]

  given JsonCodec[Session] =
    DeriveJsonCodec.gen[Session]
