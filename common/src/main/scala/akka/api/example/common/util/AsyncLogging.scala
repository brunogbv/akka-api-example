package akka.api.example.common.util

import akka.actor.ActorSystem
import akka.event.LoggingAdapter

/**
  * Created by bvalerio on 15/02/2019.
  */
trait AsyncLogging {
  protected def log(implicit actorSystem: ActorSystem): LoggingAdapter =
    akka.event.Logging.getLogger(actorSystem, this)
}