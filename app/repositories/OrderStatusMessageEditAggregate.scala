package repositories

import models.{OrderStatusMessageText, OrderStatus, Language, OrderStatusMessage}
import scalikejdbc._

case class OrderStatusMessageEdit(message: OrderStatusMessage, data: List[(String, Option[OrderStatusMessageText])])
object OrderStatusMessageEditAggregate {
  def get(id: Long): Option[OrderStatusMessageEdit]={
    implicit val session = AutoSession
    val orderStatusMess = OrderStatusMessage.find(id)
    orderStatusMess.map{
      m =>
        val orderStatusMessagesText: List[(String, Option[OrderStatusMessageText])] = for(lang <- Language.findAll()) yield (lang.cod, OrderStatusMessageText.find(messageid = m.id, languageid = lang.id))
        Some(OrderStatusMessageEdit(m, orderStatusMessagesText))
    }.getOrElse(None)
  }

}
