package akka.api.example.common.model.service.meta

/**
  * Created by bvalerio on 26/06/2018.
  */

/**
 * Representation of a response from a REST api call.  Contains meta data as well as the optional
 * response payload if there was no error
 */
case class ApiResponse[T](meta:ApiResponseMeta, response:Option[T] = None)

