package com.ms.streaming.kinesis

import java.time.LocalDateTime

case class EventToProcess(
                           timestamp: LocalDateTime,
                           eventTime: String,
                           eventSource: String,
                           userName: Option[String],
                           eventName: String,
                           awsRegion: String
                         )

