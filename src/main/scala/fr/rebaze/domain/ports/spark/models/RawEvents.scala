package fr.rebaze.domain.ports.spark.models

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
