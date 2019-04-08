//package stone.hermes.api.controllers.pushgroup
//
//import java.time.LocalDateTime
//
//import akka.actor.{ActorRef, ActorSystem, Props}
//import com.typesafe.scalalogging.LazyLogging
//import stone.hermes.common.entities.EntityActor.{Delete, GetFieldsObject}
//import stone.hermes.common.entities.model.PushGroupFO
//import stone.hermes.common.service.results.FullResult
//
//import scala.concurrent.Future
//
///**
//  * Created by bvalerio on 30-Jan-18.
//  */
//
//class MerchantManagerTest extends FlatSpec with BeforeAndAfterAll with Matchers with LazyLogging with MockitoSugar {
//  implicit val system: ActorSystem = ActorSystem()
//
//  class scoping extends TestKit(system) with ImplicitSender {
//    val mockedRepo: PushGroupRepository = mock[PushGroupRepository]
//    val parent: TestProbe = TestProbe("parent")
//    val time: LocalDateTime = LocalDateTime.now()
//    val mockedMerchant: TestProbe = TestProbe("merchant-1")
//    val anotherMockedMerchant: TestProbe = TestProbe("merchant-2")
//    val stoneCodes = Vector("131192", "150559")
//    val merchantFO = PushGroupFO(1, Vector.empty[String], "groupName", "contract", "endpoint1", "header", "healthcheck",
//      isActive = true, "statusFilter", 1, LocalDateTime.MIN, LocalDateTime.MIN)
//    val anotherMerchantFO: PushGroupFO = merchantFO.copy(id = 2, groupName = "robgay", endpoint = "endpoint2",
//      isActive = false)
//    val merchantInput = MerchantInput(1, "groupName", "contract", "endpoint1", "header", "healthcheck",
//      isActive = true, "statusFilter", 1)
//    val merchantManager: ActorRef = parent.childActorOf(Props(new PushGroupController {
//      override val repo: PushGroupRepository = mockedRepo
//
//      override def lookupOrCreateChild(id: Int): ActorRef =
//        if (id > 1) anotherMockedMerchant.ref
//        else mockedMerchant.ref
//    }))
//  }
//
//  "A MerchantManager" should "list merchants on database" in new scoping() {
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    when(mockedRepo.loadEntity(2)).thenReturn(Future.successful(Some(anotherMerchantFO)))
//    when(mockedRepo.listPushGroupIds).thenReturn(Future.successful(Vector(1, 2)))
//    parent.send(merchantManager, ListMerchants)
//    mockedMerchant.expectMsg(GetFieldsObject)
//    anotherMockedMerchant.expectMsg(GetFieldsObject)
//    mockedMerchant.reply(FullResult(merchantFO))
//    anotherMockedMerchant.reply(FullResult(anotherMerchantFO))
//    parent.expectMsg(FullResult(Vector(merchantFO, anotherMerchantFO)))
//  }
//
//  it should "list merchants by name on database" in new scoping() {
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    when(mockedRepo.findPushGroupIdByName("robgay")).thenReturn(Future.successful(Some(2)))
//    parent.send(merchantManager, FindMerchantByName("robgay"))
//    anotherMockedMerchant.expectMsg(GetFieldsObject)
//    anotherMockedMerchant.reply(FullResult(anotherMerchantFO))
//    parent.expectMsg(FullResult(Vector(anotherMerchantFO)))
//  }
//
//  it should "find merchant by endpoint on database" in new scoping() {
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    when(mockedRepo.findMerchantIdByEndpoint("endpoint1")).thenReturn(Future.successful(Some(1)))
//    parent.send(merchantManager, FindMerchantByEndpoint("endpoint1"))
//    mockedMerchant.expectMsg(GetFieldsObject)
//    mockedMerchant.reply(FullResult(merchantFO))
//    parent.expectMsg(FullResult(merchantFO))
//  }
//
//  it should "insert merchant on database" in new scoping() {
//    when(mockedRepo.persistEntity(merchantFO.copy(id = 0))).thenReturn(Future.successful(1))
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    parent.send(merchantManager, InsertMerchant(merchantInput))
//    mockedMerchant.expectMsg(merchantFO.copy(id = 0))
//    mockedMerchant.reply(FullResult(merchantFO))
//    parent.expectMsg(FullResult(merchantFO))
//  }
//
//  it should "insert stoneCode for given merchant on database" in new scoping() {
//    when(mockedRepo.insertStoneCodes(1, stoneCodes)).thenReturn(Future.successful(1))
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    parent.send(merchantManager, InsertStoneCodesForMerchant(1, stoneCodes))
//    mockedMerchant.expectMsg(InsertStoneCodes(stoneCodes))
//    mockedMerchant.reply(FullResult(merchantFO.copy(stoneCodes = stoneCodes)))
//    parent.expectMsg(FullResult(merchantFO.copy(stoneCodes = stoneCodes)))
//  }
//
//  it should "delete stoneCode for given merchant on database" in new scoping() {
//    when(mockedRepo.deleteStoneCodes(1, stoneCodes)).thenReturn(Future.successful(1))
//    parent.send(merchantManager, DeleteStoneCodesForMerchant(1, stoneCodes))
//    mockedMerchant.expectMsg(DeleteStoneCodes(stoneCodes))
//    mockedMerchant.reply(FullResult(merchantFO))
//    parent.expectMsg(FullResult(merchantFO))
//  }
//
//  it should "delete merchant on database" in new scoping() {
//    when(mockedRepo.deleteEntity(1)).thenReturn(Future.successful(1))
//    parent.send(merchantManager, DeleteMerchant(1))
//    mockedMerchant.expectMsg(Delete)
//    mockedMerchant.reply(FullResult(merchantFO.copy(deleted = true)))
//    parent.expectMsg(FullResult(merchantFO.copy(deleted = true)))
//  }
//
//  it should "select merchants on database" in new scoping() {
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    parent.send(merchantManager, SelectMerchant(1))
//    mockedMerchant.expectMsg(GetFieldsObject)
//    mockedMerchant.reply(FullResult(merchantFO))
//    parent.expectMsg(FullResult(merchantFO))
//  }
//
//  override def afterAll {
//    TestKit.shutdownActorSystem(system)
//  }
//}
