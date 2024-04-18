package fr.rebaze.domain.ports.spark.models

//
case class SessionIdsByUserId(userId: String, sessionId: String, timeStamp: Long)
////object SessionIdByUserId :
////  given Encoder[SessionIdsByUserId] = Encoders.product[SessionIdsByUserId]
