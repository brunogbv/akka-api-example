package akka.api.example.common.controllers.common.routes

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem}
import akka.api.example.common.controllers.common.requests.ApiRequest
import akka.api.example.common.model.service.{ErrorMessage, Failure, FailureType, ServiceResult}
import akka.api.example.common.model.service.meta.{ApiResponse, ApiResponseMeta}
import akka.api.example.common.model.service.results.{EmptyResult, FullResult}
import akka.api.example.common.util.{AsyncLogging, LocalDateTimeHelpers}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.json4s.jackson.Serialization.write

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
  * Created by bvalerio on 25/06/2018.
  */

object RoutesDefinition extends AkkaHttpJson4sSupport {

  case class ApiResponseMetrics(msgName: String, responseStatus: Int, responseTime: Double)

  val NotFoundResp: ApiResponse[String] =
    ApiResponse[String](ApiResponseMeta(NotFound.intValue, Some(ErrorMessage("Resource not Found"))))
  val UnexpectedFailResp: ApiResponse[String] =
    ApiResponse[String](ApiResponseMeta(InternalServerError.intValue, Some(ErrorMessage.UnexpectedFailure)))
  val BadRequestResp: ApiResponse[String] =
    ApiResponse[String](ApiResponseMeta(BadRequest.intValue, Some(ErrorMessage("Bad Request"))))

  val rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handleAll[MethodRejection] { methodRejection =>
      val names = methodRejection.map(_.supported.name())
      val errorMessage = ErrorMessage(s"HTTP method not allowed, supported methods: ${names mkString ", "}")
      val apiResp = ApiResponse[String](ApiResponseMeta(MethodNotAllowed.intValue, Some(errorMessage)))
      complete((MethodNotAllowed, apiResp))
    }
      .handleAll[ValidationRejection] { validationRejection =>
      val messages = validationRejection.map(_.message)
      val errorMessage = ErrorMessage(s"Validation error: ${messages mkString ", "}")
      val apiResp = ApiResponse[String](ApiResponseMeta(BadRequest.intValue, Some(errorMessage)))
      complete((BadRequest, apiResp))
    }

      .handleAll[MalformedRequestContentRejection] { malformedRequestRejection =>
      val message = malformedRequestRejection.map(_.message)
      val errorMessage =
        ErrorMessage(s"Malformed Request: ${(message mkString ", ").replace("\n", ". ")}")
      val apiResp = ApiResponse[String](ApiResponseMeta(BadRequest.intValue, Some(errorMessage)))
      complete((BadRequest, apiResp))
    }

      .handleAll[AuthenticationFailedRejection] { _ =>
      complete(Unauthorized)
    }

      .handleAll[MissingQueryParamRejection] { missingParamRejection =>
      val paramNames = missingParamRejection.map(_.parameterName)
      val errorMessage = ErrorMessage(s"Missing parameters: ${paramNames mkString ", "}")
      val apiResp = ApiResponse[String](ApiResponseMeta(BadRequest.intValue, Some(errorMessage)))
      complete((BadRequest, apiResp))
    }
      .handleAll[Rejection](rejection => {
      val errorMessage = ErrorMessage(s"Server error: ${rejection.map(_.getClass).mkString(",")}")
      val apiResp = ApiResponse[String](ApiResponseMeta(BadRequest.intValue, Some(errorMessage)))
      complete((InternalServerError, apiResp))
    })
      .handleNotFound {
        complete((NotFound, NotFoundResp))
      }
      .result()

  case class EmptyBody()

}

abstract class RoutesDefinition(routePrefix: PathMatcher[Unit],
                                entityController: ActorRef)
                               (implicit
                                ec: ExecutionContext,
                                system: ActorSystem,
                                mat: ActorMaterializer)
  extends AkkaHttpJson4sSupport
    with AsyncLogging
    with LocalDateTimeHelpers {

  import RoutesDefinition._

  def routeFromBody[I <: ApiRequest : Manifest, O <: AnyRef](httpMethodDirective: Directive0,
                                                             routeTimeout: Timeout = Timeout(10.seconds)): Route =
    route[I, Unit, O](httpMethodDirective, (x, _) => x, routeTimeout)

  def routeFromUrl[T, O <: AnyRef](httpMethodDirective: Directive[T],
                                   parser: T => ApiRequest,
                                   routeTimeout: Timeout = Timeout(10.seconds)): Route =
    route[EmptyBody, T, O](httpMethodDirective, (_, extracted) => parser(extracted), routeTimeout)

  def route[I: Manifest, T, O <: AnyRef](httpMethodDirective: Directive[T],
                                         parser: (I, T) => ApiRequest,
                                         routeTimeout: Timeout = Timeout(10.seconds)): Route ={
    (ignoreTrailingSlash & httpMethodDirective & pathEndOrSingleSlash).tapply(extractedFromUrl =>
      manifest[I].runtimeClass.getConstructors.filter(x => x.getParameterCount == 0) match {
        case x if x.isEmpty =>
          entity(as[I]) { msg =>
            serviceAndComplete(
              parser(msg, extractedFromUrl),
              entityController
            )(routeTimeout)
          }
        case x =>
          serviceAndComplete(
            parser(x.head.newInstance().asInstanceOf[I], extractedFromUrl),
            entityController
          )(routeTimeout)
      }
    )
  }

  def entityRoutes: Seq[Route]

  /**
    * Returns the stone.odin.grafana.api.routes defined for this endpoint
    */
  final def routes: Route =
    pathPrefix(routePrefix)(entityRoutes.reduce(_ ~ _))


  /** z
    * Uses ask to send a request to an actor, expecting a ServiceResult back in return
    *
    * @param msg The message to send
    * @param ref The actor ref to send to
    * @return a Future for a ServiceResult for type T
    */
  def service[T: ClassTag](msg: Any, ref: ActorRef)
                          (serviceTimeout: Timeout): Future[ServiceResult[T]] = {
    import akka.pattern.ask
    implicit val timeout: Timeout = serviceTimeout
    val result = (ref ? msg).mapTo[ServiceResult[T]]
    result
  }


  /**
    * Uses stone.odin.common.service to get a result and then inspects that result to complete the request
    *
    * @param msg The message to send
    * @param ref The actor ref to send to
    * @return a completed Route
    */
  def serviceAndComplete[T: ClassTag](msg: Any, ref: ActorRef)
                                     (serviceTimeout: Timeout): Route = {
    log.info(s"Received message: $msg")
    val requestTimestamp = LocalDateTime.now.getEpochMillis

    val fut = service[T](msg, ref)(serviceTimeout)
    onComplete(fut) {
      //      case scala.util.Success(x: FullResult[ApiResponse[_]]) =>
      //        log.info(s"parse-json: ${write(ApiResponseMetrics(msg.toString, OK.intValue, getTimestampDifferenceInSeconds(requestTimestamp)))(org.json4s.DefaultFormats)}")
      //        complete((StatusCode.int2StatusCode(x.value.meta.statusCode), x.value))

      case scala.util.Success(FullResult(t)) =>
        val resp = ApiResponse(ApiResponseMeta(OK.intValue), Some(t))
        log.info(s"parse-json: ${write(ApiResponseMetrics(msg.toString, OK.intValue, getTimestampDifferenceInSeconds(requestTimestamp)))(org.json4s.DefaultFormats)}")
        complete(resp)

      case scala.util.Success(EmptyResult) =>
        log.info(s"parse-json: ${write(ApiResponseMetrics(msg.toString, NotFound.intValue, getTimestampDifferenceInSeconds(requestTimestamp)))(org.json4s.DefaultFormats)}")
        complete((NotFound, NotFoundResp))

      case scala.util.Success(Failure(FailureType.Validation, ErrorMessage.InvalidEntityId, _)) =>
        log.info(s"parse-json: ${write(ApiResponseMetrics(msg.toString, NotFound.intValue, getTimestampDifferenceInSeconds(requestTimestamp)))(org.json4s.DefaultFormats)}")
        complete((NotFound, NotFoundResp))

      case scala.util.Success(fail: Failure) =>
        val status = fail.failType match {
          case FailureType.Validation => BadRequest
          case FailureType.BadRequest => BadRequest
          case FailureType.Unauthorized => Unauthorized
          case FailureType.NotAcceptable => NotAcceptable
          case any: FailureType.Value =>
            log.error(fail.exception.get, s"Received the following exception while processing message $msg:\n$any")
            InternalServerError
        }
        val apiResp = ApiResponse[String](ApiResponseMeta(status.intValue, Some(fail.message)))
        log.info(s"parse-json: ${write(ApiResponseMetrics(msg.toString, status.intValue, getTimestampDifferenceInSeconds(requestTimestamp)))(org.json4s.DefaultFormats)}")
        complete((status, apiResp))

      case scala.util.Failure(e) =>
        log.error(s"Received the following exception while processing message $msg:\n${e.getMessage}")
        log.info(s"parse-json: ${write(ApiResponseMetrics(msg.toString, InternalServerError.intValue, getTimestampDifferenceInSeconds(requestTimestamp)))(org.json4s.DefaultFormats)}")
        complete((InternalServerError, UnexpectedFailResp))
      case unexpected =>
        log.error(s"Received unexpected message $unexpected while processing message $msg.")
        log.info(s"parse-json: ${write(ApiResponseMetrics(msg.toString, InternalServerError.intValue, getTimestampDifferenceInSeconds(requestTimestamp)))(org.json4s.DefaultFormats)}")
        complete((InternalServerError, UnexpectedFailResp))
    }
  }

}
