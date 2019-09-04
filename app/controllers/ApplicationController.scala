package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder

import models.auth.DefaultEnv
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents
}

import scala.concurrent.Future
class ApplicationController @Inject()(components: ControllerComponents,
                                      silhouette: Silhouette[DefaultEnv])
    extends AbstractController(components) {

  val logger: Logger = Logger(this.getClass)
  val SecuredAction: SecuredActionBuilder[DefaultEnv, AnyContent] =
    silhouette.SecuredAction

  def badPassword: Action[AnyContent] = SecuredAction.async {
    implicit request =>
      if (logger.isDebugEnabled) {
        logger.debug(
          "Displaying headers :-------------------------------------------------------------------")
        val headers: Map[String, String] = request.headers.toSimpleMap
        for ((k, v) <- headers) {
          logger.debug(s"key: $k, value: $v")
        }
      }
      Future.successful(Ok(Json.obj("result" -> "qwerty1234")))
  }

}
