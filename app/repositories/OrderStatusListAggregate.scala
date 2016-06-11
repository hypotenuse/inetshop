package repositories

import models.{OrderStatus}

case class OrderStatusAgr(id: Long,
                       sendmessageClient: Boolean,
                       sendmessageAdmin: Boolean,
                        title: String
                        )

object OrderStatusListAggregate {

  def list(): List[OrderStatusAgr] = {
    OrderStatus.findAll().flatMap{
      status=>
        status.textByDefaultLang.map(t => OrderStatusAgr(status.id, status.sendmessageClient, status.sendmessageAdmin, t.title))
    }
  }

}


