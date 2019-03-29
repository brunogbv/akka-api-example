package akka.api.example.common.controllers.common.routes

import akka.api.example.common.serialization.json4s.JsonSupport
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

/**
  * Created by bvalerio on 24/01/2019.
  */
trait AkkaHttpJson4sSupport extends JsonSupport with Json4sSupport
