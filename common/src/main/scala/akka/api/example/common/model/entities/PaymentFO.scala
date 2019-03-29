package akka.api.example.common.model.entities

import akka.api.example.common.model.EntityFieldsObject
import akka.api.example.common.model.contract.PaymentContract

/**
  * Created by bvalerio on 30-Jan-18.
  */

object PaymentFO {
  def fromInput(input: PaymentContract.Input): PaymentFO ={
    require(input.size==2,s"Can only instantiate from a single payment.")
    PaymentFO(Set(input.head._1, input.last._1).hashCode(), input.head._1, input.last._1, input.last._2)
  }
}
case class PaymentFO(id: Int,
                     payer: String,
                     receiver: String,
                     amount: Int,
                     deleted: Boolean = false)
  extends EntityFieldsObject[PaymentFO] {
  def assignId(id: Int): PaymentFO = this.copy(id = id)

  override def markDeleted: PaymentFO = this.copy(deleted = true)

  def asOutput: PaymentContract.Output =
    if(amount >=0) (payer, receiver, amount)
    else (receiver, payer, -amount)

  /** Adds two payments between same users.
    *
    * @throws IllegalArgumentException if payment is not between same users.
    * @param payment the payment to be added.
    * @return a new [[PaymentFO]] representing both this class and the parameter
    */
  def +(payment: PaymentFO): PaymentFO =
    (this.payer, this.receiver) match {
      case (payment.payer, payment.receiver) => this.copy(amount = this.amount + payment.amount)
      case (payment.receiver, payment.payer) => this.copy(amount = this.amount - payment.amount)
      case _ => throw new IllegalArgumentException("Payments can only be added if they are between same users.")
    }
}
