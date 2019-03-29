package akka.api.example.common.model.service

import akka.api.example.common.model.service.results.{EmptyResult, FullResult}

/**
  * Created by bvalerio on 30-Jan-18.
  */

/**
  * Represents a result from a call to a service actor, being either a FullResult (Some), EmptyResult (None) or a Failure.
  * Has the full completent of methods like map, flatMap and filter to treat as a Monad
  */
abstract class ServiceResult[+A] {
  def isEmpty: Boolean

  def isValid: Boolean

  def getOrElse[B >: A](default: => B): B = default

  def map[B](f: A => B): ServiceResult[B] = EmptyResult

  def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = EmptyResult

  def filter(p: A => Boolean): ServiceResult[A] = this

  def toOption: Option[A] = this match {
    case FullResult(a) => Some(a)
    case _ => None
  }
}

/**
  * Companion to ServiceResult
  */
object ServiceResult {

  def fromOption[A](opt: Option[A]): ServiceResult[A] = opt match {
    case None => EmptyResult
    case Some(value) => FullResult(value)
  }

}

