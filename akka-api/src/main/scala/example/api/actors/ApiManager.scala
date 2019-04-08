package example.api.actors

import akka.actor.ActorSystem
import akka.api.example.common.controllers.common.routes.RoutesDefinition
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.typesafe.scalalogging.LazyLogging
import ApiManager.ApiRoutes

/**
  * Created by bvalerio on 04/07/2018.
  */

object ApiManager {

  case class ApiRoutes(routesDefinitions: RoutesDefinition*)

}

trait ApiManager
  extends LazyLogging {

  //  implicit val timeout: Timeout = 100.seconds

  private val apiHost = "127.0.0.1"
  private val apiPort = 13000

  /**
    * Initializes Akka HTTP within the execution context in Settings
    *
    * @param routes Routes from all the controllers
    */
  private def initRoutes(routes: Route)
                        (implicit
                         actorSystem: ActorSystem,
                         mat: Materializer): Unit = {
    val serverSource = Http().bind(interface = apiHost, port = apiPort )
    logger.info(s"Api is listening on $apiHost:$apiPort")
    val sink = Sink.foreach[Http.IncomingConnection](_.handleWith(routes))
    serverSource.to(sink).run()
  }

  def initApi(apiRoutes: ApiRoutes)
             (implicit
              actorSystem: ActorSystem,
              mat: Materializer): Unit = {
    logger.info("Initializing API...")
    val routes: Seq[Route] = apiRoutes.routesDefinitions.map(_.routes)

    val definedRoutes: Route = routes.reduce(_ ~ _)

    val finalRoutes: Route =
      handleRejections(RoutesDefinition.rejectionHandler) {
        cors() {
          pathPrefix("api" / "v1")(definedRoutes)
        }
      }
    initRoutes(finalRoutes)
    logger.info("API initialized!")
  }
}
