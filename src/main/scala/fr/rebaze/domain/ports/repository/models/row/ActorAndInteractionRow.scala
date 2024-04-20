package fr.rebaze.domain.ports.repository.models.row

import fr.rebaze.domain.ports.repository.models.{Interaction, LevelId}
import io.getquill.JsonValue

case class ActorAndInteractionRow(
  actorGuid: String,
  levelId: LevelId,
  interaction: JsonValue[Interaction]
)
