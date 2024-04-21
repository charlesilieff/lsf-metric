package fr.rebaze.domain.ports.models

import fr.rebaze.domain.ports.repository.models.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
case class LevelsProgressByActorGuid(actorGuid: ActorGuid, progress: Map[LevelId, Double])

object LevelsProgressByActorGuid:
  given interactionZioEncoder: zio.json.JsonEncoder[LevelsProgressByActorGuid] = DeriveJsonEncoder.gen[LevelsProgressByActorGuid]
  given interactionZioDecoder: zio.json.JsonDecoder[LevelsProgressByActorGuid] = DeriveJsonDecoder.gen[LevelsProgressByActorGuid]
