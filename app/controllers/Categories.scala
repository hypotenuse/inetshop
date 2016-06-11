package controllers

import javax.inject.Inject

import models.Category
import play.Logger
import play.api.Play
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.{FileService, ImageService}
import services.auth.Authenticator

class Categories @Inject()(val messagesApi: MessagesApi, authServ: Authenticator) extends Controller with I18nSupport {


  def picture(catId: Long , extension: String) = Action { implicit request =>
    Category.find(catId).map {
      category =>
        ImageService.getPictureContent(extension, category).map{
          content =>
            Ok(content).as(FileService.getContentType(extension))
        }.getOrElse(NotFound(Messages("notfound")))
    }.getOrElse(NotFound(Messages("notfound")))

  }


  def thumb(id: Long) = Action {
    import play.api.Play.current
    import services.FileService
    val content: Option[Array[Byte]] = {for {
      c <- Category.find(id)
      p <- c.catpicture
      width <- current.configuration.getInt("thumbnail.width")
      height <- current.configuration.getInt("thumbnail.height")
    } yield {
        ImageService.getThumbContent(c, width, height, Some(p))
      }}.flatten

    content.map(c => Ok(c).as(FileService.getContentType("jpg")))
      .getOrElse(NotFound(Messages("notfound")))
  }



}
