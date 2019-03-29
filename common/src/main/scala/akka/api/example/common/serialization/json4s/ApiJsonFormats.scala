package akka.api.example.common.serialization.json4s

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.{TemporalAccessor, TemporalQuery}

import org.json4s.JsonAST.JString
import org.json4s.jackson.Serialization
import org.json4s.{CustomSerializer, Formats, NoTypeHints}
/**
  * Created by bvalerio on 11/09/2018.
  */

object ApiJsonFormats {
  class LocalDateTimeSerializer(val format: DateTimeFormatter) extends CustomSerializer[LocalDateTime](_ => (
    {
      case JString(s) => format.parse(s, asQuery(LocalDateTime.from))
    },
    {
      case t: LocalDateTime => JString(format.format(t))
    }
  ))
  //noinspection ConvertExpressionToSAM
  private def asQuery(f: TemporalAccessor => LocalDateTime): TemporalQuery[LocalDateTime] =
    new TemporalQuery[LocalDateTime] {
      override def queryFrom(temporal: TemporalAccessor): LocalDateTime = f(temporal)
    }

  val formats: Formats = Serialization.formats(NoTypeHints) +
    new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))

}
