package controllers

import javax.inject.Inject

import controllers.actionbuilders.LanguageActions._
import controllers.actionbuilders._
import models.{News => mNews}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.ImageService
import services.auth.Authenticator

class Goods @Inject()(val messagesApi: MessagesApi, authServ: Authenticator) extends Controller with I18nSupport {

  def detail(lng: String, slug: String) = (CartAction andThen LanguageAction(lng)) {
      implicit request =>
      Ok("Ok")
    }

  def list(lng: String, length: Int, start: Int) = (CartAction andThen LanguageAction(lng)) {
      implicit request =>
      Ok("Ok")
    }


}
