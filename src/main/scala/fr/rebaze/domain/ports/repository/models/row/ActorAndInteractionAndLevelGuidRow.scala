package fr.rebaze.domain.ports.repository.models.row

import fr.rebaze.domain.ports.repository.models.{Interaction,LevelId}
import io.getquill.JsonValue

case class ActorAndInteractionAndLevelGuidRow(
  actorGuid: String,
  levelGuid: LevelId,
  interaction: JsonValue[Interaction]
)
