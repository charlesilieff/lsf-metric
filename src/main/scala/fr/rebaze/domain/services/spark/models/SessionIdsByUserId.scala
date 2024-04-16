//package fr.rebaze.domain.services.spark.models
//
//import org.apache.spark.sql.{Encoder, Encoders}
//
//case class SessionIdsByUserId(userId: String, sessionId: String, timeStamp: Long)
//
//object SessionIdByUserId {
//  implicit val sessionIdByUserIdEncoder: Encoder[SessionIdsByUserId] =
//    Encoders.product[SessionIdsByUserId]
//}
