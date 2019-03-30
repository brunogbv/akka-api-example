package akka.api.example.common.model.contract

/**
  * Created by bruno on 29/03/2019.
  */
object PaymentContract {
  case class InputPayment(user: String, amount: Int) {
    def asInput: (String, Int) = user -> amount
  }

  type Input = Seq[(String, Int)]
  type Output = (String, String, Int)
}