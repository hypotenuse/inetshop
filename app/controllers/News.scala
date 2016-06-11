package controllers

import javax.inject.Inject

import controllers.actionbuilders._
import controllers.actionbuilders.LanguageActions._
import models.{News => mNews}
import play.api.Play
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.ImageService
import services.auth.Authenticator

class News @Inject()(val messagesApi: MessagesApi, authServ: Authenticator) extends Controller with I18nSupport {

  def detail(lng: String, slug: String) = (CartAction andThen LanguageAction(lng)) {
      implicit request =>
      Ok("Ok")
    }

  def list(lng: String, length: Int, start: Int) = (CartAction andThen LanguageAction(lng)) {
      implicit request =>
      Ok("Ok")
    }

  def picture(newsId: Long , extension: String) = Action { implicit request =>
    import services.FileService
    val content: Option[Array[Byte]] = for{
      news <- mNews.find(newsId)
      content <- ImageService.getPictureContent(extension, news)
    } yield content
    content.map{ c =>
            Ok(c).as(FileService.getContentType(extension))
        }.getOrElse(NotFound(Messages("notfound")))
  }

  def thumb(id: Long) = Action {
    import play.api.Play.current
    import services.FileService
    val content: Option[Array[Byte]] = {for {
      n <- mNews.find(id)
      width <- current.configuration.getInt("news.thumbnail.width")
      height <- current.configuration.getInt("news.thumbnail.height")
    } yield {
      ImageService.getThumbContent(n, width, height)
    }}.flatten

    content.map(c => Ok(c).as(FileService.getContentType("jpg")))
      .getOrElse(NotFound(Messages("notfound")))
  }



}
