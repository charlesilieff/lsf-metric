package fr.rebaze.domain.services.spark.models

//import org.apache.spark.sql.{DataFrame, Dataset, Encoder, Encoders}
case class EventsByUserId(
  // actorGuid
  userId: String,
  // guid
  event: String,
  // timestamp interaction
  timestamp: Long
)
//object EventsByUserId {
//  given eventsByUserIdEncoder: Encoder[EventsByUserId] =
//    Encoders.product[EventsByUserId]
//}
