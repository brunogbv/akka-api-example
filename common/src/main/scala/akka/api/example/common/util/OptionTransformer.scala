package akka.api.example.common.util

/**
  * Created by bvalerio on 19/02/2019.
  */
trait OptionTransformer {

  implicit class OptionImplicitConversionHelper[T](any: T) {
    def some: Option[T] = Option(any)
  }

}
