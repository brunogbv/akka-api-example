package example.api.controllers.payment

import akka.actor.{ActorRef, ActorSystem}
import akka.api.example.common.controllers.common.routes.RoutesDefinition
import akka.api.example.common.controllers.payment.requests.{ClearCache, GeneratePayments, GetPaymentStatusFromUsers}
import akka.api.example.common.controllers.payment.responses.PaymentOutputResponse
import akka.api.example.common.model.contract.ProcessManualPayments
import akka.api.example.common.model.entities.PaymentFO
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

/**
  * Created by bvalerio on 02/07/2018.
  */

class PaymentRoutes(paymentController: ActorRef)
                   (implicit ec: ExecutionContext,
                    system: ActorSystem,
                    mat: ActorMaterializer)
  extends RoutesDefinition("payments", paymentController) {

  import scala.concurrent.duration._

  def manualPaymentsRoute: Route =
    routeFromBody[ProcessManualPayments, PaymentOutputResponse](post & path("manual"))

  def generatedPaymentsRoute: Route =
    routeFromBody[GeneratePayments, PaymentOutputResponse](post & path("generate"), 40.seconds)

  def clearCacheRoute: Route =
    routeFromBody[ClearCache, String](post & path("clear-cache"))

  def getPaymentRoute: Route =
    routeFromBody[GetPaymentStatusFromUsers, PaymentFO](post)

  override def entityRoutes: Seq[Route] =
    Seq(
      manualPaymentsRoute,
      generatedPaymentsRoute,
      clearCacheRoute,
      getPaymentRoute
    )
}

