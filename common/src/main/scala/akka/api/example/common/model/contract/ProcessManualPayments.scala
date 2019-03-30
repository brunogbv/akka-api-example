package akka.api.example.common.model.contract

import akka.api.example.common.controllers.common.requests.ApiRequest
import akka.api.example.common.model.contract.PaymentContract.InputPayment

/**
  * Created by bruno on 29/03/2019.
  */
case class ProcessManualPayments(payments: Seq[InputPayment])
  extends ApiRequest