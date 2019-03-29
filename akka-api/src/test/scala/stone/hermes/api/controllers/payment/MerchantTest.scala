//package stone.hermes.api.controllers.pushgroup
//
//import java.time.LocalDateTime
//
//import akka.actor.{ActorRef, ActorSystem, Props}
//import com.typesafe.scalalogging.LazyLogging
//import stone.hermes.common.entities.EntityActor.{Delete, GetFieldsObject}
//import stone.hermes.common.entities.model.PushGroupFO
//import stone.hermes.common.service.results.FullResult
//import stone.hermes.common.service.{Failure, FailureType}
//
//import scala.concurrent.Future
//
///**
//  * Created by bvalerio on 30-Jan-18.
//  */
//
//class MerchantTest extends FlatSpec with BeforeAndAfterAll with Matchers with LazyLogging with MockitoSugar {
//  implicit val system: ActorSystem = ActorSystem()
//
//  class scoping(id: Int) extends TestKit(system) with ImplicitSender {
//    val mockedRepo: PushGroupRepository = mock[PushGroupRepository]
//    val parent: TestProbe = TestProbe("parent")
//    val time: LocalDateTime = LocalDateTime.now()
//    val stoneCodes = Vector("131192", "150559")
//    val merchantFO = PushGroupFO(1, Vector.empty[String], "groupName", "contract", "endpoint1", "header", "healthcheck",
//      isActive = true, "statusFilter", 1, LocalDateTime.MIN, LocalDateTime.MIN)
//    val merchantInput = MerchantInput(1, "groupName", "contract", "endpoint1", "header", "healthcheck",
//      isActive = true, "statusFilter", 1)
//    val merchant: ActorRef = parent.childActorOf(Props(new PushGroup(id) {
//      override val repository: PushGroupRepository = mockedRepo
//    }))
//  }
//
//  "A TransactionToRetry" should "initialize and reply with FO from database" in new scoping(1) {
//    logger.info(s"Starting test initialize and reply with FO from database")
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    parent.send(merchant, GetFieldsObject)
//    parent.expectMsg(FullResult(merchantFO))
//    //    Thread.sleep(3000)
//  }
//
//  it should "update merchant on database" in new scoping(1) {
//    logger.info("Starting test update merchant on database")
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    when(mockedRepo.findMerchantIdByEndpoint("endpoint1")).thenReturn(Future.successful(Some(1)))
//    when(mockedRepo.updateEntity(merchantFO.copy(groupName = "newName"))).thenReturn(Future.successful(1))
//    parent.send(merchant, UpdateMerchantInfo(merchantInput.copy(groupName = "newName")))
//    parent.expectMsg(FullResult(merchantFO.copy(groupName = "newName")))
//    //    Thread.sleep(3000)
//  }
//
//  it should "fail on updating a merchant with non unique endpoint" in new scoping(1) {
//    logger.info("Starting test fail on updating a merchant with non unique endpoint")
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    when(mockedRepo.findMerchantIdByEndpoint("endpoint2")).thenReturn(Future.successful(Some(2)))
//    parent.send(merchant, UpdateMerchantInfo(merchantInput.copy(endpoint = "endpoint2")))
//    parent.expectMsg(Failure(FailureType.Validation, PushGroup.endpointNotUniqueError))
//    //    Thread.sleep(3000)
//  }
//
//  it should "delete merchant on database" in new scoping(1) {
//    logger.info("Starting test delete merchant on database")
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    when(mockedRepo.deleteEntity(1)).thenReturn(Future.successful(1))
//    parent.send(merchant, Delete)
//    parent.expectMsg(FullResult(merchantFO.copy(deleted = true)))
//    //    Thread.sleep(3000)
//  }
//
//  it should "insert StoneCodes for merchant on database" in new scoping(1) {
//    logger.info("Starting test insert StoneCodes for merchant on database")
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    when(mockedRepo.existingStoneCodes(1, stoneCodes)).thenReturn(Future.successful(None))
//    when(mockedRepo.insertStoneCodes(1, stoneCodes)).thenReturn(Future.successful(1))
//    when(mockedRepo.persistEntity(merchantFO.copy(stoneCodes = stoneCodes))).thenReturn(Future.successful(1))
//    parent.send(merchant, InsertStoneCodes(stoneCodes))
//    parent.expectMsg(FullResult(merchantFO.copy(stoneCodes = stoneCodes)))
//    //    Thread.sleep(3000)
//  }
//
//  it should "fail if inserting existing stoneCodes" in new scoping(1) {
//    logger.info("Starting test fail if inserting existing stoneCodes")
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO.copy(stoneCodes = stoneCodes))))
//    when(mockedRepo.existingStoneCodes(1, stoneCodes)).thenReturn(Future.successful(Some(stoneCodes)))
//    parent.send(merchant, InsertStoneCodes(stoneCodes))
//    parent.expectMsg(Failure(FailureType.Validation, PushGroup.nonUniqueStoneCodeError(stoneCodes)))
//    //    Thread.sleep(3000)
//  }
//
//  it should "fail if inserting non-numeric stoneCode" in new scoping(1) {
//    logger.info("Starting test fail if inserting non-numeric stoneCode")
//    val newStoneCodes: Vector[String] = stoneCodes :+ "123124r"
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO)))
//    when(mockedRepo.existingStoneCodes(1, newStoneCodes)).thenReturn(Future.successful(None))
//    parent.send(merchant, InsertStoneCodes(newStoneCodes))
//    parent.expectMsg(Failure(FailureType.Validation, PushGroup.nonNumericStoneCodeError(Vector(newStoneCodes.last))))
//    //    Thread.sleep(3000)
//  }
//
//  it should "delete StoneCodes for merchant on database" in new scoping(1) {
//    logger.info("Starting test delete StoneCodes for merchant on database")
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO.copy(stoneCodes = stoneCodes))))
//    when(mockedRepo.deleteStoneCodes(1, stoneCodes)).thenReturn(Future.successful(1))
//    when(mockedRepo.existingStoneCodes(1, stoneCodes)).thenReturn(Future.successful(Some(stoneCodes)))
//    parent.send(merchant, DeleteStoneCodes(stoneCodes))
//    parent.expectMsg(FullResult(merchantFO))
//    //    Thread.sleep(3000)
//  }
//
//  it should "fail if deleting nonexistent StoneCodes for merchant on database" in new scoping(1) {
//    logger.info("Starting test fail if deleting nonexistent StoneCodes for merchant on database")
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(merchantFO.copy(stoneCodes = stoneCodes))))
//    when(mockedRepo.deleteStoneCodes(1, stoneCodes)).thenReturn(Future.successful(1))
//    when(mockedRepo.existingStoneCodes(1, stoneCodes)).thenReturn(Future.successful(None))
//    parent.send(merchant, DeleteStoneCodes(stoneCodes))
//    parent.expectMsg(Failure(FailureType.Validation, PushGroup.nonexistentStoneCodeError(stoneCodes)))
//    //    Thread.sleep(3000)
//  }
//
//}
