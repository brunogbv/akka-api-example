//package stone.hermes.transaction
//
//import java.time.LocalDateTime
//
//import akka.actor.{ActorRef, ActorSystem, Props}
//import akka.testkit.{ImplicitSender, TestKit, TestProbe}
//import com.typesafe.scalalogging.LazyLogging
//import org.mockito.Mockito._
//import org.scalatest.mockito.MockitoSugar
//import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
//import stone.hermes.common.entities.EntityActor.GetFieldsObject
//import stone.hermes.common.entities.model.PushedTransactionFO
//import stone.hermes.common.protocols.PushedTransactionInput
//import stone.hermes.pushed.transaction.management.PushedTransactionManager._
//import stone.hermes.pushed.transaction.management.{PushedTransactionManager, PushedTransactionRepository}
//import stone.hermes.common.service.results.FullResult
//
//import scala.concurrent.Future
//
///**
//  * Created by bvalerio on 30-Jan-18.
//  */
//
//class PushedTransactionManagerTest extends FlatSpec with BeforeAndAfterAll with Matchers with LazyLogging with MockitoSugar {
//  implicit val system: ActorSystem = ActorSystem()
//
//  class scoping extends TestKit(system) with ImplicitSender {
//    val mockedRepo: PushedTransactionRepository = mock[PushedTransactionRepository]
//    val parent: TestProbe = TestProbe("parent")
//    val time: LocalDateTime = LocalDateTime.now()
//    val mockedPushedTransaction: TestProbe = TestProbe("pushed-transaction-1")
//    val anotherMockedPushedTransaction: TestProbe = TestProbe("pushed-transaction-2")
//    val pushedTransactionFO = PushedTransactionFO(1, 2, 3, 20.1, time, "atk", "itk", "status", "statusDescription", "stoneCode", "legalName", "fantasyName", "type", "maskedPan", "brand", 1, "0722", "sak", "requestBody", 200, "OK", time)
//    val anotherPushedTransactionFO: PushedTransactionFO = pushedTransactionFO.copy(id = 2, amount = 2.5, idMerchant = 7)
//    val pushedTransactionInput = PushedTransactionInput(2, 3, 20.1, time, "atk", "itk", "status", "statusDescription", "stoneCode", "legalName", "fantasyName", "type", "maskedPan", "brand", 1, "0722", "sak","requestBody", 200, "OK", time)
//    val PushedTransactionManager: ActorRef = parent.childActorOf(Props(new PushedTransactionManager {
//      override val repo: PushedTransactionRepository = mockedRepo
//
//      override def lookupOrCreateChild(id: Int): ActorRef =
//        if (id > 1) anotherMockedPushedTransaction.ref
//        else mockedPushedTransaction.ref
//    }))
//  }
//
//  //  "A PushedTransactionManager" should "list pushed transactions on database" in new scoping() {
//  //    when(mockedRepo.listPushedTransactionIds).thenReturn(Future.successful(Vector(1,2)))
//  //    parent.send(PushedTransactionManager, ListPushedTransactions)
//  //    mockedPushedTransaction.expectMsg(GetFieldsObject)
//  //    anotherMockedPushedTransaction.expectMsg(GetFieldsObject)
//  //    mockedPushedTransaction.reply(FullResult(pushedTransactionFO))
//  //    anotherMockedPushedTransaction.reply(FullResult(anotherPushedTransactionFO))
//  //    parent.expectMsg(FullResult(Vector(pushedTransactionFO, anotherPushedTransactionFO)))
//  //  }
//
//  "A PushedTransactionManager" should "list pushed transactions by idMerchant on database" in new scoping() {
//    when(mockedRepo.listPushedTransactionIdsByIdMerchant(7)).thenReturn(Future.successful(Vector(2)))
//    parent.send(PushedTransactionManager, ListPushedTransactionsByIdMerchant(7))
//    anotherMockedPushedTransaction.expectMsg(GetFieldsObject)
//    anotherMockedPushedTransaction.reply(FullResult(anotherPushedTransactionFO))
//    parent.expectMsg(FullResult(Vector(anotherPushedTransactionFO)))
//  }
//
//  it should "list transactions to retry by stoneCode on database" in new scoping() {
//    when(mockedRepo.listPushedTransactionIdsByStoneCode("stoneCode")).thenReturn(Future.successful(Vector(1, 2)))
//    parent.send(PushedTransactionManager, ListPushedTransactionsByStoneCode("stoneCode"))
//    mockedPushedTransaction.expectMsg(GetFieldsObject)
//    anotherMockedPushedTransaction.expectMsg(GetFieldsObject)
//    mockedPushedTransaction.reply(FullResult(pushedTransactionFO))
//    anotherMockedPushedTransaction.reply(FullResult(anotherPushedTransactionFO))
//    parent.expectMsg(FullResult(Vector(pushedTransactionFO, anotherPushedTransactionFO)))
//  }
//
//  it should "insert transaction to retry on database" in new scoping() {
//    when(mockedRepo.persistEntity(pushedTransactionFO.copy(id = 0))).thenReturn(Future.successful(1))
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(pushedTransactionFO)))
//    parent.send(PushedTransactionManager, InsertPushedTransaction(pushedTransactionInput))
//    mockedPushedTransaction.expectMsg(pushedTransactionFO.copy(id = 0))
//    mockedPushedTransaction.reply(FullResult(pushedTransactionFO))
//    parent.expectMsg(FullResult(pushedTransactionFO))
//  }
//
//  it should "select transaction to retry on database" in new scoping() {
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(pushedTransactionFO)))
//    parent.send(PushedTransactionManager, SelectPushedTransaction(1))
//    mockedPushedTransaction.expectMsg(GetFieldsObject)
//    mockedPushedTransaction.reply(FullResult(pushedTransactionFO))
//    parent.expectMsg(FullResult(pushedTransactionFO))
//  }
//
//  override def afterAll {
//    TestKit.shutdownActorSystem(system)
//  }
//}
