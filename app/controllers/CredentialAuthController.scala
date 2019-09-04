package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{
  Clock,
  Credentials,
  PasswordHasherRegistry
}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import daos.user.UserDAO

import models.{EmailCredential, Token}
import models.auth.DefaultEnv
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents,
  Result
}

import scala.concurrent.{ExecutionContext, Future}

class CredentialAuthController @Inject()(
    components: ControllerComponents,
    userService: UserDAO,
    configuration: Configuration,
    silhouette: Silhouette[DefaultEnv],
    clock: Clock,
    credentialsProvider: CredentialsProvider,
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry,
    messagesApi: MessagesApi)(implicit ex: ExecutionContext)
    extends AbstractController(components)
    with I18nSupport {

  implicit val emailCredentialFormat: OFormat[EmailCredential] =
    Json.format[EmailCredential]

  val SecuredAction: SecuredActionBuilder[DefaultEnv, AnyContent] =
    silhouette.SecuredAction
  val authenticatorRepository: AuthenticatorService[JWTAuthenticator] =
    silhouette.env.authenticatorService
  val eventBus: EventBus = silhouette.env.eventBus

  def authenticate: Action[EmailCredential] =
    Action.async(parse.json[EmailCredential]) { implicit request =>
      val credentials = Credentials(request.body.email, request.body.password)
      val res = for {
        loginInfo <- credentialsProvider.authenticate(credentials)
        authenticator <- authenticatorRepository.create(loginInfo)
        token <- authenticatorRepository.init(authenticator)
        t <- userService.retrieve(loginInfo)
      } yield {
        val result: Result = Ok(
          Json.toJson(
            Token(token, expiresOn = authenticator.expirationDateTime)))
        t match {
          case Some(user) if !user.active =>
            Future.failed(new IdentityNotFoundException("Couldn't find user"))
          case Some(user) =>
            eventBus.publish(LoginEvent(user, request))
            authenticatorRepository.embed(token, result)
          case None =>
            Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
        result
      }
      res recover {
        case _: ProviderException =>
          Forbidden
      }
    }

  def signOut: Action[AnyContent] = SecuredAction.async { implicit request =>
    val result: Result = Ok(Json.obj("result" -> "logged out successfully"))
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorRepository.discard(request.authenticator, result)
  }
}
