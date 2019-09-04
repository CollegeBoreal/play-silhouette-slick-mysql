package daos.login

import javax.inject.{Inject, Singleton}
import models.Login
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
    implicit executionContext: ExecutionContext)
    extends LoginDTO
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val logins = lifted.TableQuery[LoginTable]

  def getAll: Future[Seq[Login]] = db.run(logins.result)

}
