package controllers.admin

import models.{Good, Picture}
import play.Logger

import play.api.libs.json._
import play.api.mvc.Action
import play.api.Play
import services.{Repository, FileService}
import jwriters.PictureWriters._
import scalikejdbc._

class Pictures extends BaseSecuredController{

  def add(goodId: Long) = admin(
    admin =>
      Action(parse.temporaryFile) { request =>
        import java.io.File
        import play.api.Play.current

        Good.find(goodId).map {
              g =>
                request.contentType match {
                  case Some("image/jpeg" | "image/gif" | "image/png") =>
                    val ext = FileService.getExtension(request.contentType.get)
                    val path = Play.application(current).path.getAbsolutePath + "/tmp/" + java.util.UUID.randomUUID.toString + "." + ext
                    request.body.moveTo(new File(path))
                    val data = FileService.getBinaryContent(path)
                    new File(path).delete()
                    data.map {
                      d =>
                        val picture = Picture.create(d, FileService.getExtension(request.contentType.get))
                        g.addPicture(picture)
                        picture.deleteThumb()
                        Ok(Json.toJson(JsNumber(picture.id)))
                    }.getOrElse(BadRequest(Json.toJson(JsObject(Map("obj.picture" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.bad.file"))))))))))

                  case _ => BadRequest(Json.toJson(JsObject(Map("obj.picture" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.unsupported.file")))))))))
                }
            }.getOrElse(NotFound)

      }
  )

  def list(goodId: Long) = admin(
    admin =>
      Action {
        Good.find(goodId).map{
          g =>
            implicit val pictureShortWrites = new Writes[(Long, String)] {
              def writes(tuple: (Long, String)) = Json.obj(
                "id" -> tuple._1,
                "extension" -> tuple._2
              )
            }
            Ok(
            Json.toJson{
            g.picturesWithoutData
          }
          )
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        Picture.find(id).map{
          p =>
            p.destroy()
            Ok(Json.toJson(Json.obj("status" -> "success")))
        }.getOrElse(NotFound)
      }
  )
}
