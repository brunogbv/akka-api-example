//package stone.hermes.user
//
//import java.time.LocalDateTime
//
//import akka.actor.{ActorRef, ActorSystem, Props}
//import akka.testkit.{ImplicitSender, TestKit, TestProbe}
//import com.typesafe.scalalogging.LazyLogging
//import org.mockito.Mockito._
//import org.scalatest.mockito.MockitoSugar
//import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
//import stone.hermes.common.entities.EntityActor.{Delete, GetFieldsObject}
//import stone.hermes.common.entities.model.UserFO
//import stone.hermes.common.service.results.FullResult
//import stone.hermes.user.management.User.{UpdatePersonalInfo, UserInput}
//import stone.hermes.user.management.UserController._
//import stone.hermes.user.management.{UserController, UserRepository}
//
//import scala.concurrent.Future
//
///**
//  * Created by bvalerio on 30-Jan-18.
//  */
//
//class UserManagerTest extends FlatSpec with BeforeAndAfterAll with Matchers with LazyLogging with MockitoSugar {
//  implicit val system: ActorSystem = ActorSystem()
//
//  class scoping extends TestKit(system) with ImplicitSender {
//    val mockedRepo: UserRepository = mock[UserRepository]
//    val parent: TestProbe = TestProbe("parent")
//    val mockedUser: TestProbe = TestProbe("user-1")
//    val anotherMockedUser: TestProbe = TestProbe("user-2")
//    val userInput = UserInput("SuperUser", "rob", "gay", "robgay")
//    val updatedUserInput = UserInput("SuperUser", "bruno", "lindo", "brunolindo")
//    val userFO = UserFO(0, "SuperUser", "rob", "gay", "robgay", LocalDateTime.MIN, LocalDateTime.MIN)
//    val userManager: ActorRef = parent.childActorOf(Props(new UserController {
//      override val repo: UserRepository = mockedRepo
//
//      override def lookupOrCreateChild(id: Int): ActorRef =
//        if (id > 1) anotherMockedUser.ref
//        else mockedUser.ref
//    }))
//  }
//
//  "A UserManager" should "list users on database" in new scoping() {
//    when(mockedRepo.listUsers).thenReturn(Future.successful(Vector(1, 2)))
//    parent.send(userManager, ListUsers)
//    mockedUser.expectMsg(GetFieldsObject)
//    anotherMockedUser.expectMsg(GetFieldsObject)
//    mockedUser.reply(FullResult(userFO.copy(id = 1)))
//    anotherMockedUser.reply(FullResult(userFO.copy(id = 2)))
//    parent.expectMsg(FullResult(Vector(userFO.copy(id = 1), userFO.copy(id = 2))))
//  }
//
//  it should "find user by username on database" in new scoping() {
//    when(mockedRepo.findUserIdByUsername("rob")).thenReturn(Future.successful(Some(1)))
//    parent.send(userManager, FindUserByUsername("rob"))
//    mockedUser.expectMsg(GetFieldsObject)
//    mockedUser.reply(FullResult(userFO.copy(id = 1)))
//    parent.expectMsg(FullResult(userFO.copy(id = 1)))
//  }
//
//  it should "sign up new user on database" in new scoping() {
//    //when(mockedRepo.persistEntity(UserFO(0, 0, "rob", "gay", "robgay", time, time))).thenReturn(Future.successful(1))
//    parent.send(userManager, SignUpNewUser(userInput))
//    mockedUser.expectMsg(userFO)
//    private val time: LocalDateTime = LocalDateTime.now()
//    private val robgay = userFO.copy(id = 1, creationDate = time, modifyDate = time)
//    mockedUser.reply(FullResult(robgay))
//    parent.expectMsg(FullResult(robgay))
//  }
//
//  it should "select user on database by id" in new scoping() {
//    parent.send(userManager, SelectUser(1))
//    mockedUser.expectMsg(GetFieldsObject)
//    mockedUser.reply(FullResult(userFO.copy(id = 1)))
//    parent.expectMsg(FullResult(userFO.copy(id = 1)))
//  }
//
//  it should "update user on database" in new scoping() {
//    when(mockedRepo.updateEntity(userFO.copy(id = 1))).thenReturn(Future.successful(1))
//    parent.send(userManager, UpdateUser(1, updatedUserInput))
//    mockedUser.expectMsg(UpdatePersonalInfo(updatedUserInput))
//    mockedUser.reply(FullResult(userFO.copy(email = updatedUserInput.username, password = updatedUserInput.password, name = updatedUserInput.name)))
//    parent.expectMsg(FullResult(userFO.copy(email = updatedUserInput.username, password = updatedUserInput.password, name = updatedUserInput.name)))
//  }
//
//  it should "delete user on database" in new scoping() {
//    when(mockedRepo.deleteEntity(1)).thenReturn(Future.successful(1))
//    parent.send(userManager, DeleteUser(1))
//    mockedUser.expectMsg(Delete)
//    mockedUser.reply(FullResult(userFO.copy(id = 1, deleted = true)))
//    parent.expectMsg(FullResult(userFO.copy(id = 1, deleted = true)))
//  }
//
//  override def afterAll {
//    TestKit.shutdownActorSystem(system)
//  }
//}