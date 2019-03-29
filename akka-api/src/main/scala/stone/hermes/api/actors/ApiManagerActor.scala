package stone.hermes.api.actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.stream.ActorMaterializer
import stone.hermes.api.controllers.payment.{PaymentController, PaymentRoutes}

/**
  * Created by bvalerio on 04/07/2018.
  */
object ApiManagerActor {
  def supervisorName: String = "api-manager-supervisor-actor"

  def propsWithSupervisor: Props = {
    import scala.concurrent.duration._
    BackoffSupervisor.props(Backoff.onStop(
      childProps = Props(new ApiManagerActor),
      childName = "api-manager-actor",
      minBackoff = 3.seconds,
      maxBackoff = 30.seconds,
      randomFactor = 0.2))
  }
}

class ApiManagerActor
  extends Actor
    with ApiManager {

  import ApiManager.ApiRoutes
  import context.{dispatcher, system}

  private implicit val mat = ActorMaterializer()(context)
  private val paymentController: ActorRef = PaymentController.initiate

  override def preStart(): Unit =
    initApi(
      ApiRoutes(
        new PaymentRoutes(paymentController)
      )
    )

  override def receive: Receive = {
    case PoisonPill =>
      context.stop(self)
  }
}
