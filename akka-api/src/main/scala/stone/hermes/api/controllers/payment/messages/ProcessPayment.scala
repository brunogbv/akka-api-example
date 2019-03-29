package stone.hermes.api.controllers.payment.messages

import akka.api.example.common.model.entities.PaymentFO

/**
  * Created by bruno on 29/03/2019.
  */
case class ProcessPayment(payment: PaymentFO)
