package akka.api.example.common.util

import akka.actor.ActorSystem

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by bvalerio on 14/02/2019.
  */
object FutureTimeout {

  case class TimeoutException() extends Exception("The future timed out.")

}

trait FutureTimeout {
  import FutureTimeout.TimeoutException

  protected implicit class FutureWithTimeoutHelperImplicitConversion[T](fut: => Future[T]) {

    def withTimeout(timeout: FiniteDuration,
                    throwable: Throwable = TimeoutException())
                   (implicit
                    actorSystem: ActorSystem,
                    ec: ExecutionContext): Future[T] =
      Future.firstCompletedOf(Seq(fut, akka.pattern.after(timeout, actorSystem.scheduler)(Future.failed(throwable))))
  }

}
