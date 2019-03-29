package stone.hermes.api.controllers.payment.util

import java.time.LocalDateTime

import akka.api.example.common.model.contract.PaymentContract
import akka.api.example.common.util.LocalDateTimeHelpers

import scala.collection.parallel.immutable.ParSeq
import scala.util.Random

/**
  * Created by bruno on 29/03/2019.
  */
//Apenas um objeto pra gerar a entrada
trait PaymentInputGenerator extends LocalDateTimeHelpers {
  private val random =
    new Random(LocalDateTime.now.getEpochMillis)

  private def generateValue(max: Int): Int =
    random.nextInt(max) + 1


  private def generatePayerAndReceiver(users: Seq[String]): (String, String) =
    (random.nextInt(users.size), random.nextInt(users.size)) match {
      case x if x._1 != x._2 => users(x._1) -> users(x._2)
      case _ => generatePayerAndReceiver(users)
    }

  private def generateUsers(numberOfUsers: Int) ={
    val seq = (for {
      i <- ParSeq.range(1, numberOfUsers + 1)
      user = s"user-$i"
    } yield user).toIndexedSeq
    assert(numberOfUsers == seq.size)
    seq
  }

  def generateInput(numberOfUsers: Int,
                    numberOfPayments: Int,
                    maximumAmount: Int): PaymentContract.Input =
    (for {
      _ <- ParSeq.range(0, numberOfPayments)
      amount = generateValue(maximumAmount)
      payerAndReceiver = generatePayerAndReceiver(generateUsers(numberOfUsers))
    } yield Seq(payerAndReceiver._1 -> -amount, payerAndReceiver._2 -> amount))
      .flatten
      .toIndexedSeq
}