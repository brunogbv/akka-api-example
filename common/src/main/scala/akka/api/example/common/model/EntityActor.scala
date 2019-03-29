package akka.api.example.common.model

/**
  * Created by bvalerio on 30-Jan-18.
  */

import akka.actor.{ActorRef, FSM, Stash, Status}
import akka.api.example.common.model.service.results.{EmptyResult, FullResult}
import akka.api.example.common.model.service.{ErrorMessage, Failure, FailureType}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.reflect.ClassTag

object EntityActor {

  case object GetFieldsObject

  case object Initialize

  case object Delete

  trait State

  case object Initializing extends State

  case object Initialized extends State

  case object Missing extends State

  case object Creating extends State

  case object Persisting extends State

  case object FailedToLoad extends State

  trait Data

  case object NoData extends Data

  case class InitializingData(id: Int) extends Data

  object NonStateTimeout {
    def unapply(any: Any): Option[Any] = any match {
      case FSM.StateTimeout => None
      case _ => Some(any)
    }
  }

  type ErrorMapper = PartialFunction[Throwable, Failure]

  case class Loaded[FO](fo: Option[FO])

  case class MissingData[FO](id: Int, deleted: Option[FO] = None) extends Data

  case class InitializedData[FO](fo: FO) extends Data

  case class PersistingData[FO](fo: FO, f: Int => FO, newInstance: Boolean = false) extends Data

  case class FinishCreate[FO](fo: FO)(implicit executionContextExecutor: ExecutionContextExecutor) {
    def map(f: FinishCreate[FO] => FinishCreate[FO]): Future[FinishCreate[FO]] = Future(this)
  }

}

abstract class EntityActor[FO <: EntityFieldsObject[FO] : ClassTag](idInput: Int)
  extends ApiActor
    with FSM[EntityActor.State, EntityActor.Data]
    with Stash
    with LazyLogging {

  import EntityActor._

  import concurrent.duration._

  val entityType: String = getClass.getSimpleName

  startWith(Initializing, InitializingData(idInput))

  when(Initializing) {

    case Event(fo: FO, _) =>
      goto(Initialized) using InitializedData(fo)

    case Event(Initialize, data: InitializingData) =>
      log.info("Initializing state data for {} {}", entityType, data.id)
      //      repository.loadEntity(data.id).map(fo => Loaded(fo)) pipeTo self
      stay

    case Event(Loaded(Some(fo)), _) =>
      unstashAll
      goto(Initialized) using InitializedData(fo)

    case Event(Loaded(None), data: InitializingData) =>
      log.error("No entity of type {} for id {}", entityType, idInput)
      unstashAll
      goto(Missing) using MissingData(data.id)

    case Event(Status.Failure(ex), data: InitializingData) =>
      log.error(ex, "Error initializing {} {}, stopping now.", entityType, data.id)
      goto(FailedToLoad) using data

    case Event(NonStateTimeout(_), _) =>
      stash
      stay
  }

  when(Missing, 1.second) {
    case Event(GetFieldsObject, data: MissingData[FO]) =>
      val result = data.deleted.map(FullResult.apply).getOrElse(EmptyResult)
      sender ! result
      stay

    case Event(NonStateTimeout(_), _) =>
      sender ! Failure(FailureType.Validation, ErrorMessage.InvalidEntityId)
      stay
  }


  when(Initialized)(standardInitializedHandling orElse initializedHandling)

  def deleteCallback(fo: FO): FSM.State[EntityActor.State, Data] = {
    requestFoForSender()
    //    persist(fo, repository.deleteEntity(fo.id), _ => fo.markDeleted)
    goto(Missing)
  }

  final def standardInitializedHandling: StateFunction = {
    case Event(GetFieldsObject, InitializedData(fo: FO)) =>
      sender ! FullResult(fo)
      stay

    case Event(Delete, InitializedData(fo: FO)) =>
      deleteCallback(fo)
  }

  def initializedHandling: StateFunction

  whenUnhandled {
    case Event(StateTimeout, _) =>
      log.info("{} entity {} has reached max idle time, stopping instance", getClass.getSimpleName, self.path.name)
      stop
  }

  def requestFoForSender(): Unit = requestFoForSender(sender())

  def requestFoForSender(ref: ActorRef): Unit = self.tell(GetFieldsObject, ref)
}