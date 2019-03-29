package akka.api.example.common.controllers.payment.responses

import akka.api.example.common.model.contract.PaymentContract

/**
  * Created by bruno on 29/03/2019.
  */
case class PaymentOutputResponse(elapsedTime: String,
                                 elapsedTimePerPayment: String,
                                 output: Seq[PaymentContract.Output])