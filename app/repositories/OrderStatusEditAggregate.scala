package repositories

import models._
import scalikejdbc._

case class OrderStatusEdit(orderStatus: OrderStatus, data: List[(String, Option[OrderStatusText])])
object OrderStatusEditAggregate {
  def get(id: Long): Option[OrderStatusEdit]={

    implicit val session = AutoSession
    val orderStatus = OrderStatus.find(id)
    orderStatus.map{
      orderStatus =>
        val OrderStatusTextData: List[(String, Option[OrderStatusText])] = for(lang <- Language.findAll()) yield (lang.cod, OrderStatusText.findBy(sqls"languageid = ${lang.id} and orderstatusid = ${id}"))
        Some(OrderStatusEdit(orderStatus, OrderStatusTextData))
    }.getOrElse(None)
  }

}
