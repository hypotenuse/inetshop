package controllers.admin

import java.util.NoSuchElementException

import jwriters.CategoryWriters._
import models._
import play.api.Logger
import jreaders.CategoryReaders._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.Action
import repositories.{CategorySelectAggregate, CategoryEditAggregate}
import scalikejdbc._
import services.{ImageService, FileService}
import validators.{JCategoryUpdate, JCategoryNew}

import scala.util.Success

class Categories extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JCategoryNew].fold(
          valid = { jCat =>
            val category=Category.create(jCat)
            Ok(Json.toJson(JsNumber(category.id)))
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
        json.validate[JCategoryUpdate].fold(
          valid = { jCat =>
            try {
              Category.save(id, jCat)
              Ok(Json.toJson(JsString("Saved")))
            }
            catch {
              case e: NoSuchElementException => NotFound
              case e: categoryParentEqualToItsChildren => BadRequest(Json.obj("msg" -> "CATEGORY_PARENT_EQUAL_TO_IT_CHILD"))
              case e: categoryParentEqualToCategory => BadRequest(Json.obj("msg" -> "CATEGORY_PARENT_EQUAL_TO_ITSELF"))
              case e: IllegalArgumentException => BadRequest(Json.toJson(JsObject(Map("obj.title" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.required")))))))))
            }
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )

      }
  )

  def list(parent: Option[Long]) = admin(
    admin =>
      Action {
        implicit request =>
          Language.findBy(sqls"defaultlng = true").map{
            l =>
              val categories: List[JsArray] = {
                val categories = parent.map{
                  pt=>
                  Category.findAllBy(sqls"parent = ${pt}")
                }.getOrElse(Category.findAllBy(sqls"parent ISNULL"))
                categories.map {
                  category => (
                    category.id,
                    category.textByLang(l.id).map(t => t.title).getOrElse(""),
                    category.onhome)
                }.sortWith((a,b) => a._2 < b._2).map{
                  agr =>
                    val (id, title, onhome) = agr
                    JsArray(
                      Seq(
                        JsNumber(id),
                        JsString(title),
                        JsBoolean(onhome)
                      ))
                }
              }
              val result = Json.obj("data"  -> categories)
              Ok(Json.toJson(result))

          }.getOrElse(Ok(Json.toJson(Json.obj("error" -> "Default language not found!"))))

      }
  )

  def select(id: Long) = admin(
    admin =>
      Action {
        implicit request =>
          Language.findBy(sqls"defaultlng = true").map{
            l =>
              Category.find(id).map{
                c =>
                  Ok(Json.toJson(CategorySelectAggregate.list(Some(c))))
              }.getOrElse(Ok(Json.toJson(Json.obj("msg" -> "Category not found!"))))

          }.getOrElse(Ok(Json.toJson(Json.obj("msg" -> "Default language not found!"))))

      }
  )

  def selectAll() = admin(
    admin =>
      Action {
        implicit request =>
          Language.findBy(sqls"defaultlng = true").map{
            l =>
                  Ok(Json.toJson(CategorySelectAggregate.list()))
          }.getOrElse(Ok(Json.toJson(Json.obj("msg" -> "Default language not found!"))))
      }
  )

  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        CategoryEditAggregate.get(id, request).map{
          categoryEdit => Ok(Json.toJson(categoryEdit))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        Category.find(id).map{
          category =>
            category.destroy() match{
              case Success(true) => Ok(Json.toJson(Json.obj("msg" -> "success")))
              case _ => BadRequest(Json.toJson(Json.obj("msg" -> "CATEGORY_HAS_CHILDREN")))
            }
        }.getOrElse(NotFound)
      }
  )

  def addPicture(categoryId: Long) = admin(
    admin =>
      Action(parse.temporaryFile) { request =>
        import java.io.File
        import play.api.Play.current
        import play.api.Play

        Category.find(categoryId).map {
          c =>
            request.contentType match {
              case Some("image/jpeg" | "image/gif" | "image/png") =>
                val ext = FileService.getExtension(request.contentType.get)
                val path = Play.application(current).path.getAbsolutePath + "/tmp/" + java.util.UUID.randomUUID.toString + "." + ext
                request.body.moveTo(new File(path))
                val data = FileService.getBinaryContent(path)
                new File(path).delete()
                data.map {
                  d =>
                    val newCat = c.copy(
                      picture = Some(d),
                      extension = Some(ext)
                    )
                    c.deletePicture()
                    newCat.save()
                    Ok(Json.toJson(JsString("SUCCESS_SAVED")))
                }.getOrElse(BadRequest(Json.toJson(JsString("INCORRECT_IMAGE_FILE"))))

              case _ => BadRequest(Json.toJson(JsString("INCORRECT_IMAGE_FILE")))
            }
        }.getOrElse(NotFound)

      }
  )


  def addCategoryPicture(categoryId: Long) = admin(
    admin =>
      Action(parse.temporaryFile) { request =>
        import java.io.File
        import play.api.Play.current
        import play.api.Play

        Category.find(categoryId).map {
          c =>
            request.contentType match {
              case Some("image/jpeg" | "image/gif" | "image/png") =>
                val ext = FileService.getExtension(request.contentType.get)
                val path = Play.application(current).path.getAbsolutePath + "/tmp/" + java.util.UUID.randomUUID.toString + "." + ext
                request.body.moveTo(new File(path))
                val data = FileService.getBinaryContent(path)
                new File(path).delete()
                data.map {
                  d =>
                    val newCat = c.copy(
                      catpicture = Some(d)
                    )
                    c.deleteThumb()
                    newCat.save()
                    Ok(Json.toJson(JsString("SUCCESS_SAVED")))
                }.getOrElse(BadRequest(Json.toJson(JsString("INCORRECT_IMAGE_FILE"))))

              case _ => BadRequest(Json.toJson(JsString("INCORRECT_IMAGE_FILE")))
            }
        }.getOrElse(NotFound)

      }
  )

  def deletePicture(categoryId: Long) = admin(
    admin =>
        Action {
          Category.find(categoryId).map{
            c =>
              c.copy(picture = None, extension = None).save()
              c.deletePicture()
              Ok(Json.toJson("SAVED"))
          }.getOrElse(NotFound)
        }
  )


  def deleteCategoryPicture(categoryId: Long) = admin(
    admin =>
        Action {
          Category.find(categoryId).map{
            c =>
              c.copy(catpicture = None).save()
              c.deleteThumb()
              Ok(Json.toJson("SAVED"))
          }.getOrElse(NotFound)
        }
  )

}
