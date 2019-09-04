package daos.password

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import daos.login.LoginDAO
import javax.inject.{Inject, Singleton}
import models.{Login, Password}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

@Singleton
class PasswordDAO @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    loginDao: LoginDAO)(implicit val classTag: ClassTag[PasswordInfo])
    extends DelegableAuthInfoDAO[PasswordInfo]
    with PasswordDTO
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val passwords = lifted.TableQuery[PasswordTable]

  def getAll: Future[Seq[Password]] = db.run(passwords.result)

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
    db.run {
      for {
        Some(fields) <- passwords
          .filter(_.password === loginInfo.providerKey)
          .result
          .map(_.headOption)
      } yield
        Some(
          PasswordInfo(hasher = fields.hasher,
                       password = fields.secret,
                       salt = fields.salt))
    }

  override def add(loginInfo: LoginInfo,
                   authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run {
        loginQuery(loginInfo).result >>
          (passwords += Password(password = loginInfo.providerKey,
                                 hasher = authInfo.hasher,
                                 secret = authInfo.password,
                                 salt = authInfo.salt)) >>
          passwords.filter(_.password === loginInfo.providerKey).result
      }
      .map(_ => authInfo)

  override def update(loginInfo: LoginInfo,
                      authInfo: PasswordInfo): Future[PasswordInfo] =
    save(loginInfo, authInfo)

  override def save(loginInfo: LoginInfo,
                    authInfo: PasswordInfo): Future[PasswordInfo] =
    db.run {
        for (cs <- joinAction(loginInfo).map(_.head))
          yield
            cs match {
              case (_, Some(oldAuthInfo)) =>
                passwords
                  .filter(_.password === oldAuthInfo.password)
                  .map(c => (c.hasher, c.secret, c.salt))
                  .update((authInfo.hasher, authInfo.password, authInfo.salt))
              case (_, None) =>
                passwords +=
                  Password(loginInfo.providerKey,
                           hasher = authInfo.hasher,
                           secret = authInfo.password,
                           salt = authInfo.salt)
            }
      }
      .map(_ => authInfo)

  override def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run {
        for { (_, Some(oldAuthInfo)) <- joinAction(loginInfo).map(_.head) } yield
          passwords.filter(_.password === oldAuthInfo.password).delete
      }
      .map(_ => ())

  /*
  --TODO Credentials Provider is always at 1
   */
  private def loginQuery(
      loginInfo: LoginInfo): Query[loginDao.LoginTable, Login, Seq] =
    loginDao.logins.filter(fields =>
      fields.providerId === "1" && fields.providerKey === loginInfo.providerKey)

  private def joinAction(
      loginInfo: LoginInfo): DBIO[Seq[(Login, Option[Password])]] =
    (loginQuery(loginInfo) joinLeft passwords on (_.providerKey === _.password)).result

}
