package akka.api.example.common.model

import akka.actor.{ActorRef, Props}
import akka.api.example.common.model.service.ServiceResult
import akka.api.example.common.model.service.results.FullResult
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
  * Created by bvalerio on 30-Jan-18.
  */

abstract class EntityAggregate[FO <: EntityFieldsObject[FO], E <: EntityActor[FO] : ClassTag]
  extends ApiActor
    with LazyLogging {

  def lookupOrCreateChild(id: Int): ActorRef = {
    val name = entityActorName(id)
    context.child(name).getOrElse {
      log.info("Creating new {} actor to handle a request for id {}", entityName, id)
      context.actorOf(entityProps(id), name)
    }
  }

  def askForFo(entityActor: ActorRef): Future[ServiceResult[FO]] = {
    import akka.pattern.ask

    import concurrent.duration._
    implicit val timeout: akka.util.Timeout = Timeout(5.seconds)
    (entityActor ? EntityActor.GetFieldsObject).mapTo[ServiceResult[FO]]
  }

  def multiEntityLookup(f: => Future[Vector[Int]])(implicit ex: ExecutionContext): Future[ServiceResult[Seq[FO]]] = {
    for {
      ids <- f
      actors = ids.map(lookupOrCreateChild)
      fos <- Future.traverse(actors)(askForFo)
    } yield {
      FullResult(fos.flatMap(_.toOption))
    }
  }


  def entityProps(id: Int): Props

  private def entityName = {
    val entityTag = implicitly[ClassTag[E]]
    entityTag.runtimeClass.getSimpleName
  }

  private def entityActorName(id: Int) = {
    s"${entityName.toLowerCase}-$id"
  }

}