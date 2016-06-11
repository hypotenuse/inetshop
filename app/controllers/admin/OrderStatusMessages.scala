package controllers.admin


import java.util.NoSuchElementException

import jreaders.OrderStatusMessageReaders._
import jwriters.OrderStatusMessageWriters._
import models.OrderStatusMessage
import play.api.libs.json._
import play.api.mvc.Action
import repositories.{OrderStatusMessageListAggregate, OrderStatusMessageEditAggregate}
import validators.{JOrderStatusMessageNew, JOrderStatusMessageUpdate}

class OrderStatusMessages extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JOrderStatusMessageNew].fold(
          valid = { jOrderStatusMessage =>
            val orderStatusMessage=OrderStatusMessage.create(jOrderStatusMessage)
            orderStatusMessage.map{m =>
              Ok(Json.toJson(JsNumber(m.id)))
            }.getOrElse(InternalServerError)

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
        json.validate[JOrderStatusMessageUpdate].fold(
          valid = { jOrderStatusMessageUpdate =>
            val mess: Either[Exception ,OrderStatusMessage] = OrderStatusMessage.save(id, jOrderStatusMessageUpdate)
            mess match {
              case Right(_) => Ok(Json.toJson(JsString("Saved")))
              case Left(e: IllegalArgumentException) => BadRequest(Json.toJson(JsString("FOR_NEW_ELEMENT_TITLE_AND_TEXT_REQUIRED")))
              case Left(e: NoSuchElementException) => NotFound
              case Left(_) => InternalServerError
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
        val result = Json.obj("data" -> OrderStatusMessageListAggregate.list())
        Ok(Json.toJson(result))
      }
  )


  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        OrderStatusMessageEditAggregate.get(id).map{
          statusMessageEdit => Ok(Json.toJson(statusMessageEdit))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        OrderStatusMessage.find(id).map{m =>
              m.destroy()
              Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )


}
