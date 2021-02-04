package com.ms.streaming.kinesis

import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

object CloudTrailRecord {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  // https://docs.aws.amazon.com/awscloudtrail/latest/userguide/cloudtrail-event-reference-user-identity.html

  implicit val userIdentityReads: Reads[UserIdentity] = (
    (JsPath \ "type").readNullable[String] and
      (JsPath \ "principalId").readNullable[String] and
      (JsPath \ "arn").readNullable[String] and
      (JsPath \ "accountId").readNullable[String] and
      (JsPath \ "accessKeyId").readNullable[String] and
      (JsPath \ "userName").readNullable[String]
    ) (UserIdentity.apply _)

  implicit val messageReads: Reads[Message] = (
    (JsPath \ "eventVersion").read[String] and
      (JsPath \ "userIdentity").read[UserIdentity] and
      (JsPath \ "eventTime").read[String] and
      (JsPath \ "eventSource").read[String] and
      (JsPath \ "eventName").read[String] and
      (JsPath \ "awsRegion").read[String] and
      (JsPath \ "sourceIPAddress").readNullable[String] and
      (JsPath \ "eventID").read[String]
    ) (Message.apply _)

  implicit val logEventsReads: Reads[LogEvents] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "timestamp").read[Long] and
      (JsPath \ "message").read[String].map { msg =>
        Json.parse(msg).validate[Message] match {
          case s: JsSuccess[Message] => s.get
          case e: JsError =>
            logger.error(s"Message Parse Error: ${msg}")
            throw new Exception(s"Json Parse Error: ${e.errors}")
        }
      }
    ) (LogEvents.apply _)

  implicit val cloudTrailEventReads: Reads[CloudTrailEvent] = (
    (JsPath \ "messageType").read[String] and
      (JsPath \ "owner").read[String] and
      (JsPath \ "logGroup").read[String] and
      (JsPath \ "logStream").read[String] and
      (JsPath \ "subscriptionFilters").read[Seq[String]] and
      (JsPath \ "logEvents").read[Seq[LogEvents]]
    ) (CloudTrailEvent.apply _)


  /**
   *
   * @param messageType
   * @param owner
   * @param logGroup
   * @param logStream
   * @param subscriptionFilters
   * @param logEvents
   */
  case class CloudTrailEvent(
                              messageType: String,
                              owner: String,
                              logGroup: String,
                              logStream: String,
                              subscriptionFilters: Seq[String],
                              logEvents: Seq[LogEvents],
                            )

  /**
   *
   * @param id
   * @param timestamp
   * @param message
   */
  case class LogEvents(
                        id: String,
                        timestamp: Long,
                        message: Message,
                      )

  /**
   *
   * @param eventVersion
   * @param userIdentity
   * @param eventTime
   * @param eventSource
   * @param eventName
   * @param awsRegion
   * @param sourceIPAddress
   * @param eventID
   */
  case class Message(eventVersion: String,
                     userIdentity: UserIdentity,
                     eventTime: String,
                     eventSource: String,
                     eventName: String,
                     awsRegion: String,
                     sourceIPAddress: Option[String],
                     eventID: String,
                    )

  /**
   *
   * @param userIdentityType
   * @param principalId
   * @param arn
   * @param accountId
   * @param accessKeyId
   * @param userName
   */
  case class UserIdentity(userIdentityType: Option[String],
                          principalId: Option[String],
                          arn: Option[String],
                          accountId: Option[String],
                          accessKeyId: Option[String],
                          userName: Option[String],
                         )

}
