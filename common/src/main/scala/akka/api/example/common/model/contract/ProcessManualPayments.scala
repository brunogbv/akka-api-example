package akka.api.example.common.model.contract

import akka.api.example.common.controllers.common.requests.ApiRequest

/**
  * Created by bruno on 29/03/2019.
  */
case class ProcessManualPayments(payments: PaymentContract.Input)
  extends ApiRequest