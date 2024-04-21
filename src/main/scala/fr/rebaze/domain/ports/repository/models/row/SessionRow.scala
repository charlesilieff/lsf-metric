package fr.rebaze.domain.ports.repository.models.row

import fr.rebaze.domain.ports.repository.models.*
import io.getquill.JsonValue

case class SessionRow(
                       guid: String,
                       actorGuid: ActorGuid,
                       levelGuid: LevelId,
                       interaction: JsonValue[Interaction]
                     )
