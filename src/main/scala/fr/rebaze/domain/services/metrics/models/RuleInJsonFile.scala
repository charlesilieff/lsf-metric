package fr.rebaze.domain.services.metrics.models

import fr.rebaze.domain.ports.repository.models.RuleId
import zio.json.{DeriveJsonCodec, JsonCodec}



case class Rule(guid: RuleId)

case class Level(rules: List[Rule])

case class Application(levels: List[Level])

object Application {
  given JsonCodec[Rule]        =
    DeriveJsonCodec.gen[Rule]
  given JsonCodec[Level]       =
    DeriveJsonCodec.gen[Level]
  given JsonCodec[Application] =
    DeriveJsonCodec.gen[Application]
}
