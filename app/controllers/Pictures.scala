package controllers

import javax.inject.Inject

import models.Picture
import play.api.Play
import play.api.mvc.{Controller, Action}
import services.{ImageService, FileService}
import play.api.i18n.{Messages, I18nSupport, MessagesApi}

class Pictures @Inject()(val messagesApi: MessagesApi)extends Controller with I18nSupport{


  def thumb(id: Long) = Action {
    import play.api.Play.current
    import services.FileService
    val content: Option[Array[Byte]] = {for {
      p <- Picture.find(id)
      width <- current.configuration.getInt("thumbnail.width")
      height <- current.configuration.getInt("thumbnail.height")
    } yield {
        ImageService.getThumbContent(p, width, height, Some(p.data))
      }}.flatten

    content.map(c => Ok(c).as(FileService.getContentType("jpg")))
      .getOrElse(NotFound(Messages("notfound")))
  }

  def view(id: Long, extension: String) = Action {
    Picture.find(id).map {
      p =>
        if(p.extension == extension)
          Ok(p.data).as(FileService.getContentType(p.extension))
        else
          NotFound(Messages("notfound"))
    }.getOrElse(NotFound(Messages("notfound")))
  }


}
