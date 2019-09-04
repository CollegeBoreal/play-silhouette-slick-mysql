package daos.user

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import javax.inject.{Inject, Singleton}
import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
    implicit executionContext: ExecutionContext)
    extends UserDTO
    with IdentityService[User]
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val users = lifted.TableQuery[UserTable]

  def getAll: Future[Seq[User]] = db.run(users.result)

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    db.run(users.filter(_.password === loginInfo.providerKey).result)
      .map(_.headOption)

  def add(user: User): Future[Int] = db.run(users += user)

}
