package example.api

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import example.api.actors.ApiManagerActor

/**
  * Created by bvalerio on 03/07/2018.
  */
object AkkaApiExampleMain extends App {
  private val system = ActorSystem("AkkaApiExample",ConfigFactory.load())
  system.actorOf(ApiManagerActor.propsWithSupervisor, ApiManagerActor.supervisorName)
}
