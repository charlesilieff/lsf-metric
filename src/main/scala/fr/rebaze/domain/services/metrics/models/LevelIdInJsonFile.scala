package fr.rebaze.domain.services.metrics.models

import fr.rebaze.domain.ports.repository.models.LevelId
import zio.json.{DeriveJsonCodec, JsonCodec}

case class Level(guid: LevelId)

case class Application(levels: List[Level])

object Application {
  given JsonCodec[Level]       =
    DeriveJsonCodec.gen[Level]
  given JsonCodec[Application] =
    DeriveJsonCodec.gen[Application]
}
