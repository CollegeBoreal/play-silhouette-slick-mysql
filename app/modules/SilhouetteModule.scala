package modules

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.actions.{
  SecuredErrorHandler,
  UnsecuredErrorHandler
}
import com.mohiva.play.silhouette.api.crypto.{
  Crypter,
  CrypterAuthenticatorEncoder
}
import com.mohiva.play.silhouette.api.repositories.{
  AuthInfoRepository,
  AuthenticatorRepository
}
import com.mohiva.play.silhouette.api.services.{
  AuthenticatorService,
  AvatarService,
  IdentityService
}
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{
  Environment,
  EventBus,
  Silhouette,
  SilhouetteProvider
}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.services.GravatarService
import com.mohiva.play.silhouette.impl.util.{
  PlayCacheLayer,
  SecureRandomIDGenerator
}
import com.mohiva.play.silhouette.password.{
  BCryptPasswordHasher,
  BCryptSha256PasswordHasher
}
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import daos.authenticator.AuthenticatorDAO
import daos.login.LoginDAO
import daos.password.PasswordDAO
import daos.user.UserDAO
import models.User
import models.auth.{
  CustomSecuredErrorHandler,
  CustomUnsecuredErrorHandler,
  DefaultEnv
}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global

class SilhouetteModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())

    // set your own Environment [Type]
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    // @provides provideEnvironment [Implementation]
    bind[IdentityService[User]].to[UserDAO]
    // @provides provideAuthenticatorService
    bind[AuthenticatorRepository[JWTAuthenticator]].to[AuthenticatorDAO]
  }

  @Provides
  @Named("password-repository")
  def providePasswordDAO(dbConfigProvider: DatabaseConfigProvider,
                         loginDao: LoginDAO): PasswordDAO =
    new PasswordDAO(dbConfigProvider, loginDao)

  /**
    * Provides the HTTP layer implementation.
    *
    * @param client Play's WS client.
    * @return The HTTP layer implementation.
    */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
    * Provides the Silhouette environment.
    *
    * @param userService          The user service implementation.
    * @param authenticatorService The authentication service implementation.
    * @param eventBus             The event bus instance.
    * @return The Silhouette environment.
    */
  @Provides
  def provideEnvironment(
      userService: UserDAO,
      @Named("authenticator-service")
      authenticatorService: AuthenticatorService[JWTAuthenticator],
      eventBus: EventBus): Environment[DefaultEnv] =
    Environment[DefaultEnv](userService, authenticatorService, Seq(), eventBus)

  /**
    * Provides the crypter for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the authenticator.
    */
  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying
      .as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
    * Provides the authenticator service.
    *
    * Note:  If play.http.secret.key is not set in application.conf
    *        the Authenticator Service may throw :
    *        com.nimbusds.jose.KeyLengthException: The secret length must be at least 256 bits
    *        This works: scala> "01234567890123456789012345678901".getBytes.size
    *        A minimum of 32 characters is required to generate a token
    *
    *
    * @param crypter              The crypter implementation.
    * @param idGenerator          The ID generator implementation.
    * @param configuration        The Play configuration.
    * @param clock                The clock instance.
    * @return The authenticator service.
    */
  @Provides
  @Named("authenticator-service")
  def provideAuthenticatorService(
      @Named("authenticator-crypter") crypter: Crypter,
      idGenerator: IDGenerator,
      configuration: Configuration,
      clock: Clock,
      dbConfigProvider: DatabaseConfigProvider)
    : AuthenticatorService[JWTAuthenticator] = {
    val settings = JWTAuthenticatorSettings(
      sharedSecret = configuration.get[String]("play.http.secret.key"))
    val encoder = new CrypterAuthenticatorEncoder(crypter)
    val authenticatorRepository = new AuthenticatorDAO(dbConfigProvider)

    new JWTAuthenticatorService(settings,
                                Some(authenticatorRepository),
                                encoder,
                                idGenerator,
                                clock)
  }

  /**
    * Provides the password hasher registry.
    *
    * @return The password hasher registry.
    */
  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(),
                           Seq(new BCryptPasswordHasher()))
  }

  /**
    * Provides the auth info repository.
    *
    * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
    * @return The auth info repository instance.
    */
  @Provides
  def provideAuthInfoRepository(
      @Named("password-repository")
      passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository =
    new DelegableAuthInfoRepository(passwordInfoDAO)

  /**
    * Provides the credentials provider.
    *
    * @param authInfoRepository The auth info repository implementation.
    * @param passwordHasherRegistry The password hasher registry.
    * @return The credentials provider.
    */
  @Provides
  def provideCredentialsProvider(
      authInfoRepository: AuthInfoRepository,
      passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

  /**
    * Provides the avatar service.
    *
    * @param httpLayer The HTTP layer implementation.
    * @return The avatar service implementation.
    */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService =
    new GravatarService(httpLayer)
}
