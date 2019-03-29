package akka.api.example.common.controllers.payment.requests

import akka.api.example.common.controllers.common.requests.ApiRequest

/**
  * Created by bruno on 29/03/2019.
  */
case class GeneratePayments(numberOfUsers: Int,
                            numberOfPayments: Int,
                            maximumAmount: Int)
  extends ApiRequest
