package akka.api.example.common.model.service.results

import akka.api.example.common.model.service.ServiceResult

/**
  * Created by bvalerio on 30-Jan-18.
  */

/**
  * Full (positive) representation of a service call result.  This will wrap the result of a call to service to qualify
  * to the receiver that the call was successful
  */
final case class FullResult[+A](value: A) extends ServiceResult[A] {
  def isValid: Boolean = true

  def isEmpty: Boolean = false

  override def getOrElse[B >: A](default: => B): B = value

  override def map[B](f: A => B): ServiceResult[B] = FullResult(f(value))

  override def filter(p: A => Boolean): ServiceResult[A] = if (p(value)) this else EmptyResult

  override def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = f(value)
}
