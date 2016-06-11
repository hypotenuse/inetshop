package controllers.admin

import java.util.NoSuchElementException

import jreaders.OrderStatusReaders._
import jwriters.OrderStatusWriters._
import models.{OrderStatus}
import play.api.libs.json._
import play.api.mvc.Action
import repositories.{OrderStatusListAggregate, OrderStatusEditAggregate}
import services.FileService
import validators.{JOrderStatusNew, JOrderStatusUpdate}

class OrderStatuses extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JOrderStatusNew].fold(
          valid = { jOrderStatusNew =>
            val orderStatus=OrderStatus.create(jOrderStatusNew)
            Ok(Json.toJson(JsNumber(orderStatus.id)))
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
        json.validate[JOrderStatusUpdate].fold(
          valid = { statusUpdate =>
            try {
              OrderStatus.save(id, statusUpdate)
              Ok(Json.toJson(JsString("Saved")))
            }
            catch {
              case e: NoSuchElementException => NotFound
              case e: IllegalArgumentException => BadRequest(Json.toJson(JsString("No language with this language code")))
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
        val result = Json.obj("data" -> OrderStatusListAggregate.list())
        Ok(Json.toJson(result))
      }
  )


  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        OrderStatusEditAggregate.get(id).map{
          n => Ok(Json.toJson(n))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        OrderStatus.find(id).map{
          orderStatus =>
              orderStatus.destroy()
              Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )


}
