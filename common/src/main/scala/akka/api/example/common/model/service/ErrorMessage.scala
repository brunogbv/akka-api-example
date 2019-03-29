package akka.api.example.common.model.service

import akka.http.scaladsl.model.StatusCodes

/**
  * Created by bvalerio on 30-Jan-18.
  */

/**
  * Represents an error message from a failure with a service call.  Has fields for the code of the error
  * as well as a description of the error
  */
case class ErrorMessage(code: String, shortText: Option[String] = None, params: Option[Map[String, String]] = None)

/**
  * Companion to ErrorMessage
  */
object ErrorMessage {
  /**
    * Common error where an operation is requested on an entity that does not exist
    */
  val InvalidEntityId = ErrorMessage("invalid.entity.id", Some("No matching entity found"))
  val Unauthorized = ErrorMessage("Unauthorized", Some("Authentication is possible but has failed or not yet been provided."))
  val OperationNotPermitted = ErrorMessage("Operation not permitted", Some("User not allowed to execute this operation"))
  val BadGateway = ErrorMessage(StatusCodes.BadGateway.defaultMessage, Some(StatusCodes.BadGateway.reason))
  val UnexpectedFailure = ErrorMessage("common.unexpect", Some("An unexpected exception has occurred"))
}