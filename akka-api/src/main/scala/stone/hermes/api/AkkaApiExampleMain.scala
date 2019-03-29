package stone.hermes.api

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import stone.hermes.api.actors.ApiManagerActor

/**
  * Created by bvalerio on 03/07/2018.
  */
object AkkaApiExampleMain extends App {
  private val system = ActorSystem("AkkaApiExample",ConfigFactory.load())
  system.actorOf(ApiManagerActor.propsWithSupervisor, ApiManagerActor.supervisorName)
}
