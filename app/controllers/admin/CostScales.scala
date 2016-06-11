package controllers.admin

import jreaders.CostScaleReaders._
import jwriters.CostScaleWriters._
import models.CostScale
import play.api.libs.json._
import play.api.mvc.Action
import validators.{JCostScaleNew, JCostScaleUpdate}

class CostScales extends BaseSecuredController {


  def add = admin(
    admin =>
      Action(parse.json) { implicit request =>
        val json = request.body
        json.validate[JCostScaleNew].fold(
          valid = { jCostScaleNew =>
            val costScale = CostScale.create(jCostScaleNew)
            costScale.map { s =>
              Ok(Json.toJson(JsNumber(s.id)))
            }.getOrElse(BadRequest(Json.toJson(JsString("SCALE_RANGE_CLUTTERS_WITH_EXISTING_RANGE"))))
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )
      }
  )

  def update(id: Long) = admin(
    admin =>
      Action(parse.json) { implicit request =>
        val json = request.body
        json.validate[JCostScaleUpdate].fold(
          valid = { jCostScale =>
            CostScale.save(id, jCostScale).map { s =>
              Ok(Json.toJson(JsString("Saved")))
            }.recover{
              case e: IllegalArgumentException => BadRequest(Json.toJson(JsString("SCALE_RANGE_CLUTTERS_WITH_EXISTING_RANGE")))
              case e: NoSuchElementException => NotFound
            }.get
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
        val result = Json.obj("data" -> CostScale.findAll())
        Ok(Json.toJson(result))
      }
  )


  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        CostScale.find(id).map {
          costScale => Ok(Json.toJson(costScale))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        CostScale.find(id).map {
          costScale =>
            costScale.destroy()
            Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )

}
