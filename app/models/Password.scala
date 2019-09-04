package models

case class Password(password: String = "",
                    hasher: String,
                    secret: String,
                    salt: Option[String] = None)
