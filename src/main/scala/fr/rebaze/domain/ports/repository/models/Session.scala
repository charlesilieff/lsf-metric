package fr.rebaze.domain.ports.repository.models

import fr.rebaze.domain.ports.repository.models.LevelId
import zio.json.{DeriveJsonCodec, DeriveJsonDecoder, DeriveJsonEncoder, JsonCodec}

case class Session(guid: String, actorGuid: String, levelGuid: LevelId, interaction: Interaction)

object Session:
  given sessionZioEncoder: zio.json.JsonEncoder[Session] = DeriveJsonEncoder.gen[Session]
  given sessionZioDecoder: zio.json.JsonDecoder[Session] = DeriveJsonDecoder.gen[Session]

  given JsonCodec[Session] =
    DeriveJsonCodec.gen[Session]
