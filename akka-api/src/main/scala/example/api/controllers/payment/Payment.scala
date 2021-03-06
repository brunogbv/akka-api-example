package example.api.controllers.payment

import akka.actor.Props
import akka.api.example.common.controllers.payment.requests.{ClearCache, GetPaymentStatusFromUsers}
import akka.api.example.common.model.EntityActor
import akka.api.example.common.model.EntityActor.InitializedData
import akka.api.example.common.model.entities.PaymentFO
import akka.api.example.common.model.service.results.FullResult
import example.api.controllers.payment.messages.ProcessPayment

import scala.util.Try

/**
  * Created by bvalerio on 30-Jan-18.
  */
object Payment {
  def props(id: Int) = Props(new Payment(id))
}

class Payment(id: Int) extends EntityActor[PaymentFO](id) {

  private def processPayment(fo: PaymentFO,
                             msg: ProcessPayment): Int = {
    (fo.payer, fo.receiver) match {
      case (msg.payment.payer, msg.payment.receiver) =>
        fo.amount + msg.payment.amount
      case (msg.payment.receiver, msg.payment.payer) =>
        fo.amount - msg.payment.amount
      case _ =>
        throw new IllegalArgumentException(s"Wrongly received payment to process: ${msg.payment}. This FO: $fo")
    }
  }

  def initializedHandling: StateFunction = {
    case Event(msg: ProcessPayment, data: InitializedData[PaymentFO]) =>
        Try(processPayment(data.fo, msg)) match {
          case scala.util.Success(newAmount) =>
            stay using data.copy(fo = data.fo.copy(amount = newAmount))
          case scala.util.Failure(e) =>
            log.error(e, s"Received exception while processing message $msg.")
            stay
        }

    case Event(_: GetPaymentStatusFromUsers, data: InitializedData[PaymentFO]) =>
      sender ! FullResult(data.fo.asOutput)
      stay

    case Event(_: ClearCache, data: InitializedData[PaymentFO]) =>
      stay using data.copy(fo = data.fo.copy(amount = 0))

    case _ => stay
  }
}
