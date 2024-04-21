package fr.rebaze.domain.ports.spark

import fr.rebaze.domain.ports.spark.models.{EventsByUserId, Interaction, RawEvents, SessionIdsByUserId}
import org.apache.spark.sql.execution.datasources.csv.CSVFileFormat
import org.apache.spark.sql.functions.*
import org.apache.spark.sql.types.{BooleanType, LongType, StringType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}
import scala3encoders.{derivation, given}
//
import java.time.LocalDate
//
object RawDataTransformation:
  val schemaInteraction = new StructType()
    .add("ruleId", StringType, true)
    .add("correct", BooleanType, true)
    .add("timestamp", LongType, true)
    .add("exerciseId", StringType, true)

  def getEventsByUserId(json: DataFrame): Dataset[EventsByUserId]            = json
    .withColumn("interaction", from_json(col("interaction"), schemaInteraction))
    .as[RawEvents]
    .map(d =>
      EventsByUserId(
        d.actorGuid,
        d.guid,
        d.interaction.timestamp
      ))
//
  def countSession(dataFrame: Dataset[SessionIdsByUserId]): DataFrame        =
    dataFrame.groupBy("userId").agg(countDistinct("sessionId").as("sessionCount"))
//
  def averageSessionTime(dataFrame: Dataset[SessionIdsByUserId]): DataFrame  =
    dataFrame
      .groupBy("userId", "sessionId")
      .agg(min("timestamp").alias("min"), max("timestamp").alias("max"))
      .withColumn("duration", col("max") - col("min"))
      .groupBy("userId")
      .agg(
        avg("duration")
          .as("averageSessionTime")
      )
//
  def firstAndLastSession(dataFrame: Dataset[SessionIdsByUserId]): DataFrame =
    dataFrame
      .groupBy("userId")
      .agg(
        (min("timestamp") / 1000).cast("TimeStamp").alias("firstSession"),
        (max("timestamp") / 1000).cast("TimeStamp").alias("lastSession")
      )
      .withColumn("firstSession", col("firstSession"))
      .withColumn("lastSession", col("lastSession"))
//
  def writeToCSV(dataFrame: DataFrame, path: String): Unit                   = {
    val todayFormatted =
      LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val fileName       = s"$todayFormatted - User et session"
    dataFrame
      .write
      .format(classOf[CSVFileFormat].getName)
      .option("header", "true")
      .option("charset", "UTF-8")
      .save(s"$path/$fileName")
  }
