package fr.rebaze.domain.services.spark

import fr.rebaze.domain.services.spark.GetSessionIdsByUserId.runTransformation
import fr.rebaze.domain.services.spark.RawDataTransformation.*
import fr.rebaze.domain.services.spark.models.EventsByUserId
import org.apache.spark.sql.functions.*
import org.apache.spark.sql.{Dataset, SparkSession}
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

  val eventSequence                     = spark
    .read
    .format("jdbc")
    .option("url", "jdbc:postgresql://localhost:35432/backendquery")
    .option("dbtable", "public.sessioninteractionswithautoincrementid")
    .option("user", "username")
    .option("password", "password")
    .load()
  println("guid|       actorGuid|           levelGuid|         interaction")
  eventSequence.show()
//  // 5 minutes in milliseconds
  val TIME_OUT                          = 300000
//
  val sessions: Dataset[EventsByUserId] = getEventsByUserId(eventSequence)
//
  println("userId|event|timestamp")
//
  val sesssionIds2                      = runTransformation(TIME_OUT)(sessions)
//
  println(
    "userId|               event|    timestamp|time_diff|  event_seq|ts_diff_flag|sessionInteger|        sessionId"
  )
  sesssionIds2.show()

  val countSessionDF = countSession(sesssionIds2)

  val averageSessionTimeDF = averageSessionTime(sesssionIds2)

  val firstAndLastSessionDF = firstAndLastSession(sesssionIds2)

  val all        =
    countSessionDF.join(averageSessionTimeDF, "userId").join(firstAndLastSessionDF, "userId")
  val withOrigin = all
    .withColumn(
      "origin",
      when(col("userId").contains("lsf"), "lsf")
        .when(col("userId").contains("voltaire"), "voltaire")
        .otherwise("unknown")
    )
    .withColumn("userId", regexp_replace(col("userId"), "@(lsf|voltaire)", ""))
//  withOrigin.show()
//
//  writeToCSV(withOrigin, "src/main/resources/data/")
