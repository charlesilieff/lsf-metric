package fr.rebaze.domain.ports.repository.models.row

import fr.rebaze.domain.ports.repository.models.Interaction
import io.getquill.JsonValue

case class ActorAndInteractionRow(
  actorGuid: String,
  interaction: JsonValue[Interaction]
)
