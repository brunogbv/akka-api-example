package akka.api.example.common.model.service.results

import akka.api.example.common.model.service.ServiceResult

/**
  * Created by bvalerio on 30-Jan-18.
  */

/**
  * Empty (negative) representation of a service call result.  For example, if looking up
  * an entity by id and it wasn't there this is the type of response to use
  */
abstract class Empty extends ServiceResult[Nothing] {
  def isValid: Boolean = false

  def isEmpty: Boolean = true
}