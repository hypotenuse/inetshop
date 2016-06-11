package jwriters

import models.{OrderStatusMessageText, OrderStatusMessage}
import play.api.libs.json.{Writes, _}
import repositories.{OrderStatusMessageEdit, OrderStatusMessageListAggregate}


object OrderStatusMessageWriters {

  implicit val ordStatusMessageWrites = new Writes[OrderStatusMessage] {
    def writes(stMess: OrderStatusMessage) = Json.obj(
      "id" -> stMess.id,
      "forclient" -> stMess.forclient,
      "orderstatusid" -> stMess.orderstatusid
    )
  }

  implicit val ordStatusMessageTextWrites = new Writes[OrderStatusMessageText] {
    def writes(stMess: OrderStatusMessageText) = Json.obj(
      "language" -> stMess.languageid,
      "messagetitle" -> stMess.messagetitle,
      "messagetext" -> stMess.messagetext
    )
  }

  implicit val OrderStatusMessageEditWrites = new Writes[OrderStatusMessageEdit] {
    def writes(orderStatusMessageEdit: OrderStatusMessageEdit) = Json.obj(
      "message" -> Json.toJson(orderStatusMessageEdit.message),
      "data" -> Json.toJson(
        orderStatusMessageEdit.data.toMap
      )
    )
  }

  implicit val OrderStatusMessageListWrites = new Writes[OrderStatusMessageListAggregate] {
    def writes(orderStatMessageList: OrderStatusMessageListAggregate) =
          Json.obj(
            "id" -> JsNumber(orderStatMessageList.id),
            "orderstatusmessagetitle" -> JsString(orderStatMessageList.title),
            "orderstatustitle" -> JsString(orderStatMessageList.orderStatusTitle),
            "forclient" -> JsBoolean(orderStatMessageList.forclient)
        )
  }
}
