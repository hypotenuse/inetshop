package controllers.admin

import java.util.NoSuchElementException

import jreaders.ManufacturerReaders._
import jwriters.ManufacturerWriters._
import models.{Manufacturer, Language}
import play.api.libs.json._
import play.api.mvc.Action
import repositories.ManufacturerEditAggregate
import scalikejdbc._
import validators.{JManufacturerNew, JManufacturerUpdate}

class Manufacturers extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JManufacturerNew].fold(
          valid = { jMan =>
            val manufacturer=Manufacturer.create(jMan)
            Ok(Json.toJson(JsNumber(manufacturer.id)))
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
        json.validate[JManufacturerUpdate].fold(
          valid = { jMan =>
            try {
              Manufacturer.save(id, jMan)
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

  def list() = admin(
    admin =>
      Action {
        implicit request =>
          Language.findBy(sqls"defaultlng = true").map{
            l =>
              val manufacturers: List[JsArray] = {
                val manufacturers = Manufacturer.findAll()
                manufacturers.map {
                  manufacturer => (
                    manufacturer.id,
                    manufacturer.textByLang(l.id).map(t => t.title).getOrElse(""))
                }.sortWith((a,b) => a._2 < b._2).map{
                  agr =>
                    val (id, title) = agr
                    JsArray(
                      Seq(
                        JsNumber(id),
                        JsString(title)
                      ))
                }
              }
              val result = Json.obj("data"  -> manufacturers)
              Ok(Json.toJson(result))

          }.getOrElse(Ok(Json.toJson(Json.obj("error" -> "Default language not found!"))))

      }
  )

  def view(id: Long) = admin(
    admin =>
      Action {
        ManufacturerEditAggregate.get(id).map{
          a => Ok(Json.toJson(a))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        Manufacturer.find(id).map{
          manufacturer =>
            manufacturer.destroy()
            Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )
}
