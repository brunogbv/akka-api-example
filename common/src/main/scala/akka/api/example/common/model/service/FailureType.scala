package akka.api.example.common.model.service

/**
  * Created by bvalerio on 30-Jan-18.
  */

/**
  * Represents the type of failure encountered by the app
  */
object FailureType extends Enumeration {
  val Validation,
  Service,
  BadRequest,
  Unauthorized,
  OperationNotPermitted,
  NotAcceptable,
  BadGateway
  = Value
}

