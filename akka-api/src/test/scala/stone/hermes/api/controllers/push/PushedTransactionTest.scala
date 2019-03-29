//package stone.hermes.transaction
//
//import java.time.LocalDateTime
//
//import akka.actor.{ActorRef, ActorSystem, Props}
//import akka.testkit.{ImplicitSender, TestKit, TestProbe}
//import com.typesafe.scalalogging.LazyLogging
//import org.mockito.Mockito.when
//import org.scalatest.mockito.MockitoSugar
//import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
//import stone.hermes.common.entities.EntityActor.{Delete, GetFieldsObject}
//import stone.hermes.common.entities.model.PushFO
//import stone.hermes.common.protocols.PushedTransactionInput
//import stone.hermes.pushed.transaction.management.{PushedTransaction, PushedTransactionRepository}
//import stone.hermes.common.service.results.FullResult
//import stone.hermes.common.service.{Failure, FailureType}
//
//import scala.concurrent.Future
//
///**
//  * Created by bvalerio on 30-Jan-18.
//  */
//
//class PushedTransactionTest extends FlatSpec with BeforeAndAfterAll with Matchers with LazyLogging with MockitoSugar {
//  implicit val system: ActorSystem = ActorSystem()
//
//  class scoping(id: Int) extends TestKit(system) with ImplicitSender {
//    val mockedRepo: PushedTransactionRepository = mock[PushedTransactionRepository]
//    val parent: TestProbe = TestProbe("parent")
//    val time: LocalDateTime = LocalDateTime.now()
//    val pushedTransactionFO = PushFO(1, 2, 3, 20.1, time, "atk", "itk", "status","statusDescription", "stoneCode", "legalName", "fantasyName", "type", "maskedPan", "brand", 1, "0722", "sak", "requestBody", 200, "OK", time)
//    val pushedTransactionInput = PushedTransactionInput(2, 3, 20.1, time, "atk", "itk", "status","statusDescription", "stoneCode", "legalName", "fantasyName", "type", "maskedPan", "brand", 1, "0722", "sak", "requestBody", 200, "OK", time)
//    val pushedTransaction: ActorRef = parent.childActorOf(Props(new PushedTransaction(id) {
//      override val repo: PushedTransactionRepository = mockedRepo
//    }))
//  }
//
//  "A PushedTransaction" should "initialize and reply with FO from database" in new scoping(1) {
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(pushedTransactionFO)))
//    parent.send(pushedTransaction, GetFieldsObject)
//    parent.expectMsg(FullResult(pushedTransactionFO))
//  }
//
//  it should "fail if deleting pushed transaction on database" in new scoping(1) {
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(pushedTransactionFO)))
//    when(mockedRepo.deleteEntity(1)).thenReturn(Future.successful(1))
//    parent.send(pushedTransaction, Delete)
//    parent.expectMsg(Failure(FailureType.Validation, PushedTransaction.unpermittedOperationError))
//  }
//}
