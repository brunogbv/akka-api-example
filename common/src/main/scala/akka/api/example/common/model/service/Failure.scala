package akka.api.example.common.model.service

import akka.api.example.common.model.service.results.Empty

/**
  * Created by bvalerio on 30-Jan-18.
  */


/**
  * Failed (negative) result from a call to a service with fields for what type as well as the error message
  * and optionally a stack trace
  */
sealed case class Failure(failType: FailureType.Value, message: ErrorMessage, exception: Option[Throwable] = None) extends Empty {
  type A = Nothing

  override def map[B](f: A => B): ServiceResult[B] = this

  override def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = this
}