package com.ms.streaming.kinesis

import com.ms.streaming.kinesis.CloudTrailRecord.CloudTrailEvent
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer
import org.apache.flink.streaming.connectors.kinesis.config.{AWSConfigConstants, ConsumerConfigConstants}
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Timestamp
import java.time.ZoneId
import java.util.Properties

object KinesisFlinkConsumer {

  val logger: Logger = LoggerFactory.getLogger(getClass)


  def main(args: Array[String]) {

    logger.info("KinesisFlinkConsumer - Job Started")


    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val consumerConfig = new Properties()
    consumerConfig.put(AWSConfigConstants.AWS_REGION, "us-east-1")
    //    consumerConfig.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "LATEST")
    consumerConfig.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, "TRIM_HORIZON")


    val kinesisSource = new FlinkKinesisConsumer[CloudTrailEvent](
      "all_region_trail_stream", new CloudTrailKinesisDeserializationSchema(), consumerConfig)

    // ---  Event Processing

    val kinesisStream: DataStream[CloudTrailEvent] = env.addSource(kinesisSource)


    val kinesisStream1: DataStream[EventToProcess] = kinesisStream
      .flatMap(_.logEvents)
      .filter(_.message.userIdentity.userName.isDefined)
      .map(m =>
        EventToProcess(
          new Timestamp(m.timestamp).toInstant.atZone(ZoneId.of("UTC")).toLocalDateTime,
          m.message.eventTime,
          m.message.eventSource,
          m.message.userIdentity.userName,
          m.message.eventName,
          m.message.awsRegion,
        ))
      .filter(_.eventName == "ConsoleLogin")

    kinesisStream1.addSink(new PrintSinkFunction[EventToProcess])

    env.execute()
  }
}
