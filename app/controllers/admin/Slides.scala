package controllers.admin

import javax.inject.Inject

import formvalidators.SlideValidator
import models.Slide
import play.api.mvc.Action
import play.api.data.Forms._
import play.api.data._
import repositories.SlidesListAggregate
import jwriters.SlideListAggregateWriters.slideWrites
import services.{UrlService, FileService}
import play.api.libs.json._
import play.api.i18n.{I18nSupport, MessagesApi}

class Slides @Inject() (val messagesApi: MessagesApi) extends BaseSecuredController with I18nSupport{
  val uploadForm = Form(mapping(
    "url" -> optional(text(minLength = 5)))(SlideValidator.apply)(SlideValidator.unapply))

  def add = admin(
    admin =>
      Action(parse.multipartFormData) { implicit request =>
        uploadForm.bindFromRequest().fold(
          hasErrors => BadRequest(hasErrors.errorsAsJson),
          slide => {
            request.body.file("picture") match {
              case Some(file) =>
                import java.io.File
                file.contentType match {
                  case Some("image/jpeg" | "image/gif" | "image/png") =>
                    new File(FileService.tempPath).mkdirs()
                    val path = FileService.tempPath + file.filename
                    file.ref.moveTo(new File(path))
                    val data = FileService.getBinaryContent(path)
                    new File(path).delete()
                    data.map {
                      d =>
                        val slidem = Slide.create(d, slide.url, FileService.getExtension(file.contentType.get))
                        Ok(Json.toJson(JsNumber(slidem.id)))
                    }.getOrElse(InternalServerError("Can't get content from this file"))

                  case _ => BadRequest(Json.toJson(JsObject(Map("obj.picture" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.unsupported.file")))))))))
                }
              case _ => BadRequest(Json.toJson(JsObject(Map("obj.picture" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.required")))))))))
            }
          }
        )
      })


  def update(id: Long) = admin(
    admin =>
      Action(parse.multipartFormData) { implicit request =>
        val sl = Slide.find(id)
        sl.map{
          s =>
            uploadForm.bindFromRequest().fold(
              hasErrors => BadRequest(hasErrors.errorsAsJson),
              slide =>
              {
                request.body.file("picture") match {
                  case Some(file) =>
                    import java.io.File
                    file.contentType match {
                      case Some("image/jpeg" | "image/gif" | "image/png") =>
                        new File(FileService.tempPath).mkdirs()
                        val path = FileService.tempPath + file.filename
                        file.ref.moveTo(new File(path))
                        val data = FileService.getBinaryContent(path)
                        new File(path).delete()
                        data.map {
                          d =>
                            s.save(Some(d), slide.url, Some(FileService.getExtension(file.contentType.get)))
                            s.deletePicture()
                            s.deleteThumb()
                            Ok(Json.toJson(JsString("SUCCESS_SAVED")))
                        }.getOrElse(BadRequest(Json.toJson(JsString("INCORRECT_IMAGE_FILE"))))

                      case _ => BadRequest(Json.toJson(JsString("INCORRECT_IMAGE_FILE")))
                    }
                  case _ =>
                    s.save(None, slide.url, None)
                    Ok(Json.toJson(JsString("SUCCESS_SAVED")))
                }
              }
            )
        }.getOrElse(NotFound)

      })

  def view(id: Long) = admin(
    admin =>
      Action {
        implicit request =>
        Slide.find(id).map{
          s =>
            implicit val slideWrite = new Writes[Slide] {
              def writes(slide: Slide) = Json.obj(
                "id" -> JsNumber(slide.id),
                "url" -> JsString(slide.url.getOrElse("/")),
                "picture" -> JsString(slide.thumbUrl(UrlService.baseUrl))
              )
            }
            Ok(Json.toJson(s))
        }.getOrElse(NotFound)
      }
  )

    def list = admin(
      admin =>
        Action { implicit request =>
          val result = Json.obj("data" -> SlidesListAggregate.list(UrlService.baseUrl(request)))
          Ok(result)
        }
    )

    def delete(id: Long) = admin(
      admin =>
        Action {
          Slide.find(id).map{
            slide =>
              slide.destroy()
              Ok(Json.toJson(Json.obj("status" -> "success")))
          }.getOrElse(NotFound)
        }
    )

}
