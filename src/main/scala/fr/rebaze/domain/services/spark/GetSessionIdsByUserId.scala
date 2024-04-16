package fr.rebaze.domain.services.spark

import fr.rebaze.domain.services.spark.models.{EventsByUserId, SessionIdsByUserId}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.*
import org.apache.spark.sql.{Column, Dataset}
import scala3encoders.given
import scala3udf.Udf as udf

object GetSessionIdsByUserId:
  def runTransformation(timeOut: Integer)(eventSequence: Dataset[EventsByUserId]): Dataset[SessionIdsByUserId] = {

    val windowDef = Window.partitionBy("userId").orderBy("timestamp")
    val previous  = lag(eventSequence("timestamp"), 1).over(windowDef)

    val dfTSDiff =
      eventSequence.withColumn("time_diff", eventSequence("timestamp") - previous)

    def handleDiff(timeOut: Int) =
      udf((timeDiff: Long, timestamp: Long) => if (timeDiff > timeOut) timestamp + ";" else timestamp + "")

    val next       =
      dfTSDiff.withColumn("event_seq", handleDiff(timeOut)(col("time_diff"), col("timestamp")))
    next.show()
    val windowSpec =
      Window.partitionBy("userId").orderBy("timestamp").rowsBetween(Long.MinValue, 0)

    def genSessionId(userId: Column, sessionInteger: Column): Column =
      when(sessionInteger.isNull, concat(userId, lit("0")))
        .otherwise(concat(userId, sessionInteger.cast("String")))

    def genTSFlag(timeOut: Integer) = udf((timeDiff: Long) => if (timeDiff > timeOut) 1 else 0)

    val withSessionId = next
      .withColumn("ts_diff_flag", genTSFlag(timeOut)(col("time_diff")))
      .withColumn(
        "sessionInteger",
        sum("ts_diff_flag").over(windowSpec)
      )
      .withColumn("sessionId", genSessionId(col("userId"), col("sessionInteger")))
    withSessionId.show()
    withSessionId.as[SessionIdsByUserId]
  }
