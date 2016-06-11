package controllers

import javax.inject.Inject

import models.Slide
import play.api.Play
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.{ImageService, FileService}

class Slides @Inject()(val messagesApi: MessagesApi)extends Controller with I18nSupport{


  def thumb(id: Long) = Action {
    import play.api.Play.current
    import services.FileService
    val content: Option[Array[Byte]] = {for {
      n <- Slide.find(id)
      width <- current.configuration.getInt("thumbnail.width")
      height <- current.configuration.getInt("thumbnail.height")
    } yield {
        ImageService.getThumbContent(n, width, height)
      }}.flatten

    content.map(c => Ok(c).as(FileService.getContentType("jpg")))
      .getOrElse(NotFound(Messages("notfound")))
  }

  def view(id: Long, extension: String) = Action {
    import play.api.Play.current
    Slide.find(id).map {
      slide =>
        if(slide.extension == extension)
          {
            import java.io.File
            new File(slide.pictureDirPath()).mkdirs()
            Ok(slide.data).as(FileService.getContentType(slide.extension))
            FileService.getBinaryContent(slide.picturePath())
              .map(content =>
                // we found the image
                Ok(content).as(FileService.getContentType(slide.extension))
              ).getOrElse {
              ImageService.generatePicture(slide.data, slide.picturePath(), slide.extension)
              FileService.getBinaryContent(slide.picturePath())
                .map(content =>
                  // we found the image
                  Ok(content).as(FileService.getContentType(slide.extension))
                ).getOrElse(NotFound(Messages("notfound")))
            }
          }
        else
          NotFound(Messages("notfound"))
    }.getOrElse(NotFound(Messages("notfound")))
  }


}
