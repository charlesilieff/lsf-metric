package fr.rebaze.domain.services.spark

import fr.rebaze.domain.services.spark.GetSessionIdsByUserId.runTransformation
import fr.rebaze.domain.services.spark.RawDataTransformation.*
import fr.rebaze.domain.services.spark.models.{EventsByUserId, UserSessionsTime}
import org.apache.spark.sql.functions.*
import org.apache.spark.sql.{Dataset, SparkSession}
import scala3encoders.given

import java.time.LocalTime
import scala.concurrent.duration.{Duration, SECONDS}

object Run:
  val spark = SparkSession
    .builder()
    .config("spark.driver.host", "localhost")
    .appName("DataFrames Basics")
    .config("spark.master", "local")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")
//  val eventSequence = spark
//    .read
//    .parquet(
//      "/Users/charles/Code/Perso/lsf-metrics/sessioS3/1"
//    )

  val eventSequence = spark
    .read
    .format("jdbc")
    .option("url", "jdbc:postgresql://localhost:35432/backendquery")
    .option("user", "username")
    .option("password", "password")
    .option("query", "select * from public.sessioninteractionswithautoincrementid where actorguid = '8936309@voltaire'")
    .load()

  def eventSequenceByUserId(userId: String) =
    spark
      .read
      .format("jdbc")
      .option("url", "jdbc:postgresql://localhost:35432/backendquery")
      .option("user", "username")
      .option("password", "password")
      .option("query", s"select * from public.sessioninteractionswithautoincrementid where actorguid = '$userId'")
      .load()
  println("guid|       actorGuid|           levelGuid|         interaction")
  eventSequence.show()
//  // 5 minutes in milliseconds
  val TIME_OUT                              = 300000
//
  val sessions: Dataset[EventsByUserId]     = getEventsByUserId(eventSequence)
//
  println("userId|event|timestamp")
//
  val sesssionIds2                          = runTransformation(TIME_OUT)(sessions)
//
  println(
    "userId|               event|    timestamp|time_diff|  event_seq|ts_diff_flag|sessionInteger|        sessionId"
  )
  sesssionIds2.show()

  val countSessionDF = countSession(sesssionIds2)

  val averageSessionTimeDF = averageSessionTime(sesssionIds2)

  val firstAndLastSessionDF = firstAndLastSession(sesssionIds2)

  val all                                                      =
    countSessionDF.join(averageSessionTimeDF, "userId").join(firstAndLastSessionDF, "userId")
  val withOrigin                                               = all
    .withColumn(
      "origin",
      when(col("userId").contains("lsf"), "lsf")
        .when(col("userId").contains("voltaire"), "voltaire")
        .otherwise("unknown")
    )
    .withColumn("userId", regexp_replace(col("userId"), "@(lsf|voltaire)", ""))
  def getSessionTimeByUserId(userId: String): UserSessionsTime =
    val dataFrame             = eventSequenceByUserId(userId)
    val events                = getEventsByUserId(dataFrame)
    val sessionIds            = runTransformation(TIME_OUT)(events)
    val countSessionDF        = countSession(sessionIds)
    val averageSessionTimeDF  = averageSessionTime(sessionIds)
    val firstAndLastSessionDF = firstAndLastSession(sessionIds)
    val all                   =
      countSessionDF.join(averageSessionTimeDF, "userId").join(firstAndLastSessionDF, "userId").as[(String, Long, String, String, String)]
    val result                = all.collect.head
    val durationInSeconds     = LocalTime.parse(result._3).toSecondOfDay

    val userTimeSession = UserSessionsTime(result._1, result._2, Duration(durationInSeconds, SECONDS), result._4, result._5)
    println((userTimeSession.averageSessionTime * userTimeSession.sessionCount).toMinutes)
    userTimeSession
//  withOrigin.show()
//
//  writeToCSV(withOrigin, "src/main/resources/data/")
