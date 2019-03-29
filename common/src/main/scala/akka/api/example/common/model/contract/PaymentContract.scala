package akka.api.example.common.model.contract

/**
  * Created by bruno on 29/03/2019.
  */
object PaymentContract {
  type Input = Seq[(String, Int)]
  type Output = (String, String, Int)
}