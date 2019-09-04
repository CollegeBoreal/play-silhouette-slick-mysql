package daos.authenticator

import java.time.{LocalDateTime, ZoneOffset}

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.Authenticator
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatorDAO @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider)(
    implicit executionContext: ExecutionContext)
    extends AuthenticatorDTO
    with HasDatabaseConfigProvider[JdbcProfile]
    with AuthenticatorRepository[JWTAuthenticator] {

  import profile.api._

  val authenticators = lifted.TableQuery[AuthenticatorTable]

  override def find(id: String): Future[Option[JWTAuthenticator]] = db.run {
    for (auth <- authenticators.filter(_.id === id).result.map(_.headOption))
      yield
        auth match {
          case Some(x) =>
            Some(
              JWTAuthenticator(
                id,
                LoginInfo("credentials", x.key),
                new org.joda.time.DateTime(
                  x.lastUsed.toInstant(ZoneOffset.UTC).toEpochMilli),
                new org.joda.time.DateTime(
                  x.expiration.toInstant(ZoneOffset.UTC).toEpochMilli),
                None
              )
            )
          case _ => None
        }
  }

  override def add(
      authenticator: JWTAuthenticator): Future[JWTAuthenticator] = {
    db.run {
        lazy val lastUsed = LocalDateTime.now
        lazy val expiration = lastUsed.plusDays(1)
        val auth = Authenticator(1,
                                 authenticator.loginInfo.providerKey,
                                 lastUsed,
                                 expiration,
                                 0,
                                 32400,
                                 authenticator.id)
        for {
          existing <- authenticators
            .filter(fields =>
              fields.provider === 1 && fields.key === authenticator.loginInfo.providerKey)
            .result
            .headOption
          row = existing.map(
            _.copy(lastUsed = lastUsed, expiration = expiration)) getOrElse auth
          result <- authenticators.insertOrUpdate(row)
        } yield result
      }
      .map(_ => authenticator)
  }

  override def update(
      authenticator: JWTAuthenticator): Future[JWTAuthenticator] =
    db.run {
        lazy val lastUsed = LocalDateTime.now
        lazy val expiration = lastUsed.plusDays(1)

        authenticators
          .filter(fields =>
            fields.provider === 1 && fields.key === authenticator.loginInfo.providerKey)
          .map(fields => (fields.lastUsed, fields.expiration))
          .update((lastUsed, expiration))
      }
      .map(_ => authenticator)

  override def remove(id: String): Future[Unit] =
    db.run {
        authenticators
          .filter(_.id === id)
          .delete
      }
      .map(_ => ())

}
