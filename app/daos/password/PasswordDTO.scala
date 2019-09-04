package daos.password

import models.Password
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait PasswordDTO { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._
  import slick.lifted.ProvenShape

  class PasswordTable(tag: Tag) extends Table[Password](tag, "PASSWORDS") {

    // scalastyle:off magic.number
    def password: Rep[String] =
      column[String]("password", O.Length(45, varying = true), O.PrimaryKey)
    def hasher: Rep[String] = column[String]("hasher")
    def secret: Rep[String] = column[String]("secret")
    def salt: Rep[Option[String]] = column[Option[String]]("salt")
    // scalastyle:on magic.number

    // scalastyle:off method.name
    override def * : ProvenShape[Password] =
      (password, hasher, secret, salt).mapTo[Password]
    // scalastyle:on method.name

  }

}
