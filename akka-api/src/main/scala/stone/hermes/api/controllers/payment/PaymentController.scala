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

import scala.collection.parallel.immutable.ParMap
import scala.collection.parallel.mutable.ParArray
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

  private implicit val mat: ActorMaterializer = ActorMaterializer()(context)

  def entityProps(id: Int): Props = Payment.props(id)

  def processInput(input: PaymentContract.Input): (ParMap[Int, ParArray[PaymentFO]], Long) = {
    val initialTimestamp = LocalDateTime.now.getEpochMillis
    input
      .toStream
      .sliding(2, 2)
      .toParArray
      .map(x => x.head -> x.last)
      .map(x => PaymentFO.fromInput(Seq(x._1, x._2)))
      .groupBy(x => Set(x.payer, x.receiver).hashCode()) -> initialTimestamp
  }

  def processPayments(paymentsMapWithTimestamp: (ParMap[Int, ParArray[PaymentFO]], Long)): (Future[ServiceResult[Seq[PaymentFO]]], Long) = {
    Future.successful(for {
      id <- paymentsMapWithTimestamp._1.keys
      actor = lookupOrCreateChild(id)
      _ = actor ! paymentsMapWithTimestamp._1(id).head.copy(amount = 0)
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
        .single(msg.payments.map(_.asInput))
        .via(Flow.fromFunction(processInput))
        .via(Flow.fromFunction(processPayments))
        .via(Flow.fromFunction(paymentOutputResponse))
        .runWith(Sink.head)
        .flatMap(identity)
        .pipeTo(sender)

    case msg: ClearCache =>
      Future
        .successful(context.children.foreach(_ ! msg))
        .map(_ => FullResult(s"Payment cache cleared."))
        .pipeTo(sender)

    case msg: GetPaymentStatusFromUsers =>
      val id = Set(msg.user1, msg.user2).hashCode()
      val ref = lookupOrCreateChild(id)
      ref ! PaymentFO(id, msg.user1, msg.user2, 0)
      ref forward msg

    case msg =>
      logger.error(s"Received unexpected message $msg from $sender")

  }
}