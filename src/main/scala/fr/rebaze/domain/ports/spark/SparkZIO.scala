package fr.rebaze.domain.ports.spark

import fr.rebaze.domain.ports.spark.GetSessionIdsByUserId.runTransformation
import fr.rebaze.domain.ports.spark.RawDataTransformation.*
import fr.rebaze.domain.ports.spark.Spark.spark
import fr.rebaze.domain.ports.spark.models.UserSessionsTime
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.StructType
import scala3encoders.given

import java.time.LocalTime
import scala.concurrent.duration.{Duration, SECONDS}

object SparkZIO:
  def eventSequenceByUserId(userId: String): DataFrame =
    spark
      .read
      .format("jdbc")
      .option("url", "jdbc:postgresql://localhost:35432/backendquery")
      .option("user", "username")
      .option("password", "password")
      .option("query", s"select * from public.sessioninteractionswithautoincrementid where actorguid = '$userId'")
      .load()

  //  // 5 minutes in milliseconds
  val TIME_OUT = 300000

  def getSessionTimeByUserId(actorGuid: String): UserSessionsTime =
    val dataFrame             = eventSequenceByUserId(actorGuid)
    val events                = getEventsByUserId(dataFrame)
    val sessionIds            = runTransformation(TIME_OUT)(events)
    val countSessionDF        = countSession(sessionIds)
    val averageSessionTimeDF  = averageSessionTime(sessionIds)
    val firstAndLastSessionDF = firstAndLastSession(sessionIds)
    val all                   =
      countSessionDF.join(averageSessionTimeDF, "userId").join(firstAndLastSessionDF, "userId").as[(String, Long, String, String, Long)]

    val result            = all.collect.head
    val durationInSeconds = LocalTime.parse(result._3).toSecondOfDay

    val userTimeSession =
      UserSessionsTime(result._1, result._2, Duration(durationInSeconds, SECONDS), result._4, result._5)

    userTimeSession
