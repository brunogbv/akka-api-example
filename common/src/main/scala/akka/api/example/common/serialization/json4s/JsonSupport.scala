package akka.api.example.common.serialization.json4s

import org.json4s.jackson.Serialization
import org.json4s.{Formats, jackson}

/**
  * Json protocol class for the CEP entities
  */
trait JsonSupport {
  implicit val serialization: Serialization.type = jackson.Serialization
  implicit val json4sFormats: Formats = ApiJsonFormats.formats
}