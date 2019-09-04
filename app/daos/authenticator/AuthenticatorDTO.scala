package daos.authenticator

import java.sql.Timestamp
import java.time.LocalDateTime

import models.Authenticator
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait AuthenticatorDTO {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._
  import slick.lifted.ProvenShape

  class AuthenticatorTable(tag: Tag)
      extends Table[Authenticator](tag, "AUTHENTICATORS") {

    // scalastyle:off magic.number
    def provider: Rep[Int] = column[Int]("provider", O.PrimaryKey)

    def key: Rep[String] =
      column[String]("key", O.Length(45, varying = true), O.PrimaryKey)

    def lastUsed: Rep[LocalDateTime] =
      column[LocalDateTime]("lastUsed")

    def expiration: Rep[LocalDateTime] =
      column[LocalDateTime]("expiration")

    def idleTimeOut: Rep[Int] = column[Int]("idleTimeOut")

    def duration: Rep[Int] = column[Int]("duration")

    def id: Rep[String] =
      column[String]("id", O.Length(300, varying = true))

    // scalastyle:on magic.number

    // scalastyle:off method.name
    override def * : ProvenShape[Authenticator] =
      (provider, key, lastUsed, expiration, idleTimeOut, duration, id)
        .mapTo[Authenticator]

    // scalastyle:on method.name

  }

}
