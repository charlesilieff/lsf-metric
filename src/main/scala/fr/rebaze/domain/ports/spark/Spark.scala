package fr.rebaze.domain.ports.spark

import fr.rebaze.domain.ports.repository.models.ActorGuid
import fr.rebaze.domain.ports.spark.GetSessionIdsByUserId.runTransformation
import fr.rebaze.domain.ports.spark.RawDataTransformation.*
import fr.rebaze.domain.ports.spark.models.UserSessionsTime
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}
import scala3encoders.given

object Spark:
  val spark = SparkSession
    .builder()
    .config("spark.driver.host", "localhost")
    .appName("DataFrames Basics")
    .config("spark.master", "local")
    .getOrCreate()

  spark.sparkContext.setLogLevel("OFF")

  def eventSequenceByUserId(actorGuids: Iterable[ActorGuid]): DataFrame =
    val actorGuidString = actorGuids.mkString("'", "', '", "'")
    spark
      .read
      .format("jdbc")
      .option("url", "jdbc:postgresql://localhost:35432/backendquery")
      .option("user", "username")
      .option("password", "password")
      .option(
        "query",
        s"WITH actor_guids AS (SELECT unnest(ARRAY[$actorGuidString]) AS actor_guid) select * from public.sessioninteractionswithautoincrementid WHERE actorguid IN (SELECT actor_guid FROM actor_guids)"
      )
      .load()

  //  // 5 minutes in milliseconds
  val TIME_OUT = 300000

  def getSessionTimeByActorGuids(actorGuids: Iterable[ActorGuid]): Map[ActorGuid,UserSessionsTime] =
    val dataFrame            = eventSequenceByUserId(actorGuids)
    val events               = getEventsByUserId(dataFrame)
    val sessionIds           = runTransformation(TIME_OUT)(events)
    val countSessionDF       = countSession(sessionIds)
    val averageSessionTimeDF = averageSessionTime(sessionIds)

    val firstAndLastSessionDF = firstAndLastSession(sessionIds)
    val all                   =
      countSessionDF.join(averageSessionTimeDF, "userId").join(firstAndLastSessionDF, "userId").as[(String, Long, Double, String, Long)]

    val result = all.collect

    // val durationInSeconds = LocalTime.parse(result._3).toSecondOfDay

    result.map(result => (ActorGuid(result._1),UserSessionsTime( result._2, result._3.toLong, result._4, result._5))).toMap
//  def getZIOSessionTimeByUserId(userId: String): UserSessionsTime =
//    val schemaInteraction = new StructType()
//      .add("ruleId", StringType, true)
//      .add("correct", BooleanType, true)
//      .add("timestamp", LongType, true)
//      .add("exerciseId", StringType, true)
//    val zioResult         = zio
//      .spark.sql.fromSpark(sparkZIO).map(_.zioSpark
//        .withColumn("interaction", from_json(col("interaction"), schemaInteraction))).map(value => value)
////          .map(d =>
////            EventsByUserId(
////              d.actorGuid,
////              d.guid,
////              d.interaction.timestamp
////            )))
////  withOrigin.show()
////
////  writeToCSV(withOrigin, "src/main/resources/data/")
