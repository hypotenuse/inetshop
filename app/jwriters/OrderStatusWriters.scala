package jwriters

import models.{OrderStatusText, OrderStatus}
import play.api.libs.json.{Writes, _}
import repositories.{OrderStatusAgr, OrderStatusEdit}


object OrderStatusWriters {

  implicit val orderStatusWrites = new Writes[OrderStatus] {
    def writes(orderStatus: OrderStatus) = Json.obj(
      "id" -> orderStatus.id,
      "sendmessageClient" -> orderStatus.sendmessageClient,
      "sendmessageAdmin" -> orderStatus.sendmessageAdmin
    )
  }

  implicit val orderStatusTextWrites = new Writes[OrderStatusText] {
    def writes(orderStatusText: OrderStatusText) = Json.obj(
      "orderstatusid" -> orderStatusText.orderstatusid,
      "languageid" -> orderStatusText.languageid,
      "title" -> orderStatusText.title
    )
  }

  implicit val orderStatusEditWrites = new Writes[OrderStatusEdit] {
    def writes(orderStatusEdit: OrderStatusEdit) = Json.obj(
      "orderStatus" -> Json.toJson(orderStatusEdit.orderStatus),
      "data" -> Json.toJson(
        orderStatusEdit.data.toMap
      )
    )
  }

  implicit val orderStatusListWrites = new Writes[OrderStatusAgr] {
    def writes(orderStatus: OrderStatusAgr) =
      JsArray(
        Seq(
          JsNumber(orderStatus.id),
          JsString(orderStatus.title),
          JsBoolean(orderStatus.sendmessageAdmin),
          JsBoolean(orderStatus.sendmessageClient)
        ))
  }
}
