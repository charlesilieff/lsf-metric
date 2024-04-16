package fr.rebaze.domain.services.spark.models

case class RawEvents(
  actorGuid: String,
  guid: String,
  interaction: Interaction,
  levelGuid: String
)

case class Interaction(
  ruleId: String,
  correct: Boolean,
  timestamp: Long,
  exerciseId: String
)
