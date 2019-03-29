package akka.api.example.common.util

import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

/**
  * Created by bvalerio on 09-Feb-18.
  */
trait LocalDateTimeHelpers {
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

  protected def getTimestampDifferenceInSeconds(initialTimestamp: Long): Double =
    (LocalDateTime.now.getEpochMillis - initialTimestamp) / 1000D

  protected implicit class LocalDateTimeToSqlFormat(date: LocalDateTime) {
    def toSqlFormat: String = date.format(formatter)
  }

  protected implicit class LocalDateTimeToEpochMillis(date: LocalDateTime) {
    def getEpochMillis: Long = date.toInstant(ZoneOffset.UTC).toEpochMilli
  }

  protected implicit class SqlDateTimeToLocalDateTime(sqlDateString: String) {
    def toLocalDateTime: LocalDateTime = LocalDateTime.parse(sqlDateString, formatter)
  }


}