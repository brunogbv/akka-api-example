//package stone.hermes.user
//
//import java.time.LocalDateTime
//
//import akka.actor.{ActorRef, ActorSystem, Props}
//import akka.testkit.{ImplicitSender, TestKit, TestProbe}
//import com.typesafe.scalalogging.LazyLogging
//import org.mockito.Mockito.when
//import org.scalatest.mockito.MockitoSugar
//import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
//import stone.hermes.common.entities.EntityActor.GetFieldsObject
//import stone.hermes.common.entities.model.UserFO
//import stone.hermes.common.service.results.FullResult
//import stone.hermes.user.management.User.{UpdatePersonalInfo, UserInput}
//import stone.hermes.user.management.{User, UserRepository}
//
//import scala.concurrent.Future
//
///**
//  * Created by bvalerio on 30-Jan-18.
//  */
//
//class UserTest extends FlatSpec with BeforeAndAfterAll with Matchers with LazyLogging with MockitoSugar {
//  implicit val system: ActorSystem = ActorSystem()
//
//  class scoping(id: Int) extends TestKit(system) with ImplicitSender {
//    val mockedRepo: UserRepository = mock[UserRepository]
//    val parent: TestProbe = TestProbe("user-manager")
//    val user: ActorRef = parent.childActorOf(Props(new User(id) {
//      override val repo: UserRepository = mockedRepo
//    }))
//    val userInput = UserInput("SuperUser", "rob", "gay", "robgay")
//    val updatedUserInput = UserInput("SuperUser", "bruno", "lindo", "brunolindo")
//    val userFO = UserFO(id, "SuperUser", "rob", "gay", "robgay", LocalDateTime.MIN, LocalDateTime.MIN)
//    val updatedUserFO: UserFO = userFO.copy(id = id, email = updatedUserInput.username, password = updatedUserInput.password, name = updatedUserInput.name)
//  }
//
//  "A User" should "initialize and reply with FO from database" in new scoping(1) {
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(userFO)))
//    parent.send(user, GetFieldsObject)
//    parent.expectMsg(FullResult(userFO))
//  }
//
//  it should "update and reply with new FO from database" in new scoping(1) {
//    when(mockedRepo.loadEntity(1)).thenReturn(Future.successful(Some(userFO)))
//    when(mockedRepo.updateEntity(updatedUserFO)).thenReturn(Future.successful(userFO.id))
//    when(mockedRepo.findUserIdByUsername("bruno")).thenReturn(Future.successful(Some(1)))
//    parent.send(user, UpdatePersonalInfo(updatedUserInput))
//    parent.expectMsg(FullResult(updatedUserFO))
//  }
//}
