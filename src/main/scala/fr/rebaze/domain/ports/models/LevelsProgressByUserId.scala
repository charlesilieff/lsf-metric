package fr.rebaze.domain.ports.models

import fr.rebaze.domain.ports.repository.models.LevelId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
case class LevelsProgressByUserId(actorGuid: String, progress: Map[LevelId, Double])

object LevelsProgressByUserId:
  given interactionZioEncoder: zio.json.JsonEncoder[LevelsProgressByUserId] = DeriveJsonEncoder.gen[LevelsProgressByUserId]
  given interactionZioDecoder: zio.json.JsonDecoder[LevelsProgressByUserId] = DeriveJsonDecoder.gen[LevelsProgressByUserId]
