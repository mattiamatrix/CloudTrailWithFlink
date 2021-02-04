package com.ms.streaming.kinesis

import com.ms.streaming.kinesis.CloudTrailRecord.CloudTrailEvent
import com.ms.streaming.kinesis.KinesisFlinkConsumer.logger
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.streaming.connectors.kinesis.serialization.KinesisDeserializationSchema
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

/**
 *
 */
class CloudTrailKinesisDeserializationSchema extends KinesisDeserializationSchema[CloudTrailEvent] {

  /**
   * Deserialize the Kinesis event record
   * In this case, the Data attribute in a Kinesis record is Base64 encoded and compressed with the gzip format
   *
   * @param recordValue
   * @param partitionKey
   * @param seqNum
   * @param approxArrivalTimestamp
   * @param stream
   * @param shardId
   * @return
   */
  override def deserialize(recordValue: Array[Byte], partitionKey: String, seqNum: String, approxArrivalTimestamp: Long, stream: String, shardId: String): CloudTrailEvent = {

    // decompress GZIP
    val inputStream = new GZIPInputStream(new ByteArrayInputStream(recordValue))
    val stringRecord = scala.io.Source.fromInputStream(inputStream).mkString

    // logger.debug(s"stringRecord ==> ${stringRecord}")

    // convert to json
    Json.parse(stringRecord).validate[CloudTrailEvent] match {
      case s: JsSuccess[CloudTrailEvent] => s.get
      case e: JsError =>
        logger.error(s"CloudTrailEvent Parse Error: $stringRecord")
        throw new Exception(s"Json Parse Error: ${e.errors}")
    }
  }

  /**
   *
   * @return
   */
  override def getProducedType: TypeInformation[CloudTrailEvent] =
    TypeExtractor.getForClass(classOf[CloudTrailEvent])
}
