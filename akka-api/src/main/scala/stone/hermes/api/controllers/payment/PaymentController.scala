package stone.hermes.api.controllers.payment

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.api.example.common.controllers.payment.requests.{ClearCache, GeneratePayments, GetPaymentStatusFromUsers}
import akka.api.example.common.controllers.payment.responses.PaymentOutputResponse
import akka.api.example.common.model.EntityAggregate
import akka.api.example.common.model.contract.{PaymentContract, ProcessManualPayments}
import akka.api.example.common.model.entities.PaymentFO
import akka.api.example.common.model.service.ServiceResult
import akka.api.example.common.model.service.results.{EmptyResult, FullResult}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import stone.hermes.api.controllers.payment.messages.ProcessPayment
import stone.hermes.api.controllers.payment.util.PaymentInputGenerator

import scala.collection.parallel.immutable.{ParMap, ParSeq}
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by bvalerio on 30-Jan-18.
  */

object PaymentController {

  private def props: Props = Props[PaymentController]

  private def name: String = "push-group-controller-actor"

  def initiate(implicit system: ActorSystem): ActorRef =
    system.actorOf(props, name)
}

/**
  * Actor class that receives requests for User and delegates to the appropriate entity instance
  */
class PaymentController
  extends EntityAggregate[PaymentFO, Payment]
    with PaymentInputGenerator {

  import context.dispatcher

  private implicit val mat = ActorMaterializer()(context)

  def entityProps(id: Int): Props = Payment.props(id)

  def processInput(input: PaymentContract.Input): (ParMap[Int, ParSeq[PaymentFO]], Long) = {
    input
      .foldLeft(ParSeq[((String, Int), (String, Int))]())((v, e) =>
        v match {
          case x if x.isEmpty || x.head._2._2 != 0 => (e -> ("", 0)) +: x
          case x => x.head.copy(_2 = e) +: x.tail
        }
      )
      .map(x => PaymentFO.fromInput(Seq(x._1, x._2)))
      .groupBy(x => Set(x.payer, x.receiver).hashCode()) -> LocalDateTime.now.getEpochMillis
  }

  def processPayments(paymentsMapWithTimestamp: (ParMap[Int, ParSeq[PaymentFO]], Long)): (Future[ServiceResult[Seq[PaymentFO]]], Long) = {
    Future.successful(for {
      id <- paymentsMapWithTimestamp._1.keys
      actor = lookupOrCreateChild(id)
      _ = actor ! paymentsMapWithTimestamp._1(id).head
    } yield paymentsMapWithTimestamp._1(id).foreach(p => actor ! ProcessPayment(p)))
      .map { _ => multiEntityLookup(Future(paymentsMapWithTimestamp._1.keys.toVector)) }
      .flatMap(identity) -> paymentsMapWithTimestamp._2
  }

  def paymentOutputResponse(resultWithInitialTimestamp: (Future[ServiceResult[Seq[PaymentFO]]], Long)): Future[ServiceResult[PaymentOutputResponse]] = {
    resultWithInitialTimestamp._1
      .map(_.toOption)
      .map {
        case x@Some(result) =>
          FullResult(
            PaymentOutputResponse(
              getTimestampDifferenceInSeconds(resultWithInitialTimestamp._2).millis.toString(),
              (getTimestampDifferenceInSeconds(resultWithInitialTimestamp._2) / result.size).millis.toString(),
              result.par.map(_.asOutput).toIndexedSeq
            )
          )
        case _ =>
          EmptyResult
      }
  }

  override def receive: Receive = {

    case msg: GeneratePayments =>
      Source
        .single(msg)
        .via(Flow.fromFunction(x => generateInput(x.numberOfUsers, x.numberOfPayments, x.maximumAmount)))
        .via(Flow.fromFunction(processInput))
        .via(Flow.fromFunction(processPayments))
        .via(Flow.fromFunction(paymentOutputResponse))
        .runWith(Sink.head)
        .flatMap(identity)
        .pipeTo(sender)

    case msg: ProcessManualPayments =>
      Source
        .single(msg.payments)
        .via(Flow.fromFunction(processInput))
        .via(Flow.fromFunction(processPayments))
        .via(Flow.fromFunction(paymentOutputResponse))
        .runWith(Sink.head)
        .flatMap(identity)
        .pipeTo(sender)

    case _: ClearCache =>
      context.children.foreach(context.stop)
      sender ! FullResult(s"Payment cache cleared.")

    case msg: GetPaymentStatusFromUsers =>
      lookupOrCreateChild(Set(msg.users._1, msg.users._2).hashCode()) forward msg

    case msg =>
      logger.error(s"Received unexpected message $msg from $sender")

  }
}