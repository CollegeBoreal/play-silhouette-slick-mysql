package controllers

import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import daos.user.UserDAO

import models.auth.DefaultEnv
import models.{SignUp, Token, User}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsValue, Json, OFormat}
import play.api.mvc.{AbstractController, Action, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(
    components: ControllerComponents,
    userService: UserDAO,
    configuration: Configuration,
    silhouette: Silhouette[DefaultEnv],
    clock: Clock,
    credentialsProvider: CredentialsProvider,
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry,
    avatarService: AvatarService,
    messagesApi: MessagesApi)(implicit ex: ExecutionContext)
    extends AbstractController(components)
    with I18nSupport {

  implicit val signUpFormat: OFormat[SignUp] = Json.format[SignUp]

  def signUp: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val authenticatorRepository = silhouette.env.authenticatorService

    request.body
      .validate[SignUp]
      .map { signUp =>
        val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.email)
        userService.retrieve(loginInfo).flatMap {
          case None =>
            /* user not already exists */
            val user = User(number = "",
                            password = loginInfo.providerKey,
                            active = true,
                            created = java.time.LocalDateTime.now)

//            val token = UUID.randomUUID().toString.replaceAll("-", "")
            val authInfo = passwordHasherRegistry.current.hash(signUp.password)
            for {
              avatar <- avatarService.retrieveURL(signUp.email)
              _ <- authInfoRepository.add(loginInfo, authInfo)
              _ <- userService.add(user)
              authenticator <- authenticatorRepository.create(loginInfo)
              token <- authenticatorRepository.init(authenticator)
              result <- authenticatorRepository.embed(
                token,
                Ok(
                  Json.toJson(
                    Token(token = token,
                          expiresOn = authenticator.expirationDateTime))))
            } yield {
              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              silhouette.env.eventBus.publish(LoginEvent(user, request))
              result
            }
          case Some(_) =>
            /* user already exists! */
            Future(Conflict(Json.toJson("user already exists")))
        }
      }
      .recoverTotal {
        case error =>
          Future.successful(BadRequest(Json.toJson(JsError.toJson(error))))
      }
  }
}
