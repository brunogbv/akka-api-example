package akka.api.example.common.controllers.payment.requests

import akka.api.example.common.model.contract.PaymentContract

/**
  * Created by bruno on 29/03/2019.
  */
case class PaymentInput(payments: PaymentContract.Input)
