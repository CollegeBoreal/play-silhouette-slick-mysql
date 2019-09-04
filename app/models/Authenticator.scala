package models

import java.time.LocalDateTime

case class Authenticator(provider: Int,
                         key: String,
                         lastUsed: LocalDateTime,
                         expiration: LocalDateTime,
                         idleTimeOut: Int,
                         duration: Int,
                         id: String)
