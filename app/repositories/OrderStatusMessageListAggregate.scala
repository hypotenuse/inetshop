package repositories

import models.{OrderStatusMessageText, Language, OrderStatus, OrderStatusMessage}
import org.apache.commons.lang3.StringUtils

case class OrderStatusMessageListAggregate(
                                            id: Long,
                                            title: String,
                                            orderStatusTitle: String,
                                            forclient: Boolean
                                            )

object OrderStatusMessageListAggregate {
  def list(): List[OrderStatusMessageListAggregate] = {
    import scalikejdbc._
    Language.getDefault.map { l =>
      val statusMessages = OrderStatusMessage.findAll()
      statusMessages.flatMap { sm =>
        for {
          ordStatus <- OrderStatus.find(sm.orderstatusid)
          ordStatusText <- ordStatus.textByDefaultLang
          messageText <- OrderStatusMessageText.find(messageid = sm.id, languageid = l.id)
        } yield OrderStatusMessageListAggregate(sm.id, StringUtils.abbreviate(messageText.messagetitle, 30), ordStatusText.title, sm.forclient)
      }
    }.getOrElse(throw new NoSuchElementException("No default language"))

  }
}

