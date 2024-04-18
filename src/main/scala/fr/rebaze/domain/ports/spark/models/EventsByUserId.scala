package fr.rebaze.domain.ports.spark.models

//
//import org.apache.spark.sql.Encoder
//
case class EventsByUserId(
  // actorGuid
  userId: String,
  // guid
  event: String,
  // timestamp interaction
  timestamp: Long
)
//
