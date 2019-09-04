package daos.login

import models.Login
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

abstract class LoginDTO { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._
  import slick.lifted.ProvenShape

  class LoginTable(tag: Tag) extends Table[Login](tag, "LOGINS") {

    // scalastyle:off magic.number
    def providerId: Rep[String] =
      column[String]("providerId", O.Length(45, varying = true))

    def providerKey: Rep[String] =
      column[String]("providerKey", O.Length(45, varying = true), O.PrimaryKey)
    // scalastyle:on magic.number

    // scalastyle:off method.name
    override def * : ProvenShape[Login] = (providerId, providerKey).mapTo[Login]
    // scalastyle:on method.name

  }

}
