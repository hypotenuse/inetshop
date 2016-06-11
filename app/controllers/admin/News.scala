package controllers.admin

import java.util.NoSuchElementException

import jreaders.NewsReaders._
import jwriters.NewsWriters._
import models.{News => mNews}
import play.api.libs.json._
import play.api.mvc.Action
import repositories.{NewsEditAggregate, NewsListAggregate}
import services.FileService
import validators.{JNewsNew, JNewsUpdate}

class News extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JNewsNew].fold(
          valid = { jNewsNew =>
            val news=mNews.create(jNewsNew)
            Ok(Json.toJson(JsNumber(news.id)))
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )
      }
  )

  def update(id: Long) = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JNewsUpdate].fold(
          valid = { jNews =>
            try {
              mNews.save(id, jNews)
              Ok(Json.toJson(JsString("Saved")))
            }
            catch {
              case e: NoSuchElementException => NotFound
              case e: IllegalArgumentException => BadRequest(Json.toJson(JsObject(Map("obj.title" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.required")))))))))
            }
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )

      }
  )

  def list = admin(
    admin =>
      Action { implicit request =>
        import jwriters.NewsListAggregateWriters._
        val result = Json.obj("data" -> NewsListAggregate.list())
        Ok(Json.toJson(result))
      }
  )


  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        NewsEditAggregate.get(id, request).map{
          n => Ok(Json.toJson(n))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        mNews.find(id).map{
          news =>
              news.destroy()
              Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )

  def addPicture(newsId: Long) = admin(
    admin =>
      Action(parse.temporaryFile) { request =>
        import java.io.File
        import play.api.Play.current
        import play.api.Play

        mNews.find(newsId).map {
          n =>
            request.contentType match {
              case Some("image/jpeg" | "image/gif" | "image/png") =>
                val ext = FileService.getExtension(request.contentType.get)
                val path = Play.application(current).path.getAbsolutePath + "/tmp/" + java.util.UUID.randomUUID.toString + "." + ext
                request.body.moveTo(new File(path))
                val data = FileService.getBinaryContent(path)
                new File(path).delete()
                data.map {
                  d =>
                    val newNews = n.copy(
                      picture = Some(d),
                      extension = Some(FileService.getExtension(request.contentType.get))
                    )
                    newNews.save()
                    n.deletePicture()
                    Ok(Json.toJson(JsString("SUCCESS_SAVED")))
                }.getOrElse(BadRequest(Json.toJson(JsString("INCORRECT_IMAGE_FILE"))))

              case _ => BadRequest(Json.toJson(JsString("INCORRECT_IMAGE_FILE")))
            }
        }.getOrElse(NotFound)

      }
  )

  def deletePicture(newsId: Long) = admin(
    admin =>
      Action {
        mNews.find(newsId).map{
          news =>
            news.copy(picture = None, extension = None).save()
            news.deletePicture()
            Ok(Json.toJson("SAVED"))
        }.getOrElse(NotFound)
      }
  )
}
