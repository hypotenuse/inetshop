package models

import scalikejdbc._
import validators.{JOrderStatusMessageUpdate, JOrderStatusMessageNew}

import scala.util.{Failure, Try}

case class OrderStatusMessage(
                               id: Long,
                               orderstatusid: Long,
                               forclient: Boolean) {

  def save()(implicit session: DBSession = OrderStatusMessage.autoSession): OrderStatusMessage = OrderStatusMessage.save(this)(session)

  def destroy()(implicit session: DBSession = OrderStatusMessage.autoSession): Unit = OrderStatusMessage.destroy(this)(session)

}


object OrderStatusMessage extends SQLSyntaxSupport[OrderStatusMessage] {

  override val tableName = "order_status_messages"

  override val columns = Seq("id", "orderstatusid", "forclient")

  def apply(osm: SyntaxProvider[OrderStatusMessage])(rs: WrappedResultSet): OrderStatusMessage = apply(osm.resultName)(rs)

  def apply(osm: ResultName[OrderStatusMessage])(rs: WrappedResultSet): OrderStatusMessage = new OrderStatusMessage(
    id = rs.get(osm.id),
    orderstatusid = rs.get(osm.orderstatusid),
    forclient = rs.get(osm.forclient)
  )

  val osm = OrderStatusMessage.syntax("osm")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[OrderStatusMessage] = {
    withSQL {
      select.from(OrderStatusMessage as osm).where.eq(osm.id, id)
    }.map(OrderStatusMessage(osm.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[OrderStatusMessage] = {
    withSQL(select.from(OrderStatusMessage as osm)).map(OrderStatusMessage(osm.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(OrderStatusMessage as osm)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[OrderStatusMessage] = {
    withSQL {
      select.from(OrderStatusMessage as osm).where.append(where)
    }.map(OrderStatusMessage(osm.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[OrderStatusMessage] = {
    withSQL {
      select.from(OrderStatusMessage as osm).where.append(where)
    }.map(OrderStatusMessage(osm.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(OrderStatusMessage as osm).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              orderstatusid: Long,
              forclient: Boolean)(implicit session: DBSession = autoSession): OrderStatusMessage = {
    val generatedKey = withSQL {
      insert.into(OrderStatusMessage).columns(
        column.orderstatusid,
        column.forclient
      ).values(
        orderstatusid,
        forclient
      )
    }.updateAndReturnGeneratedKey.apply()

    OrderStatusMessage(
      id = generatedKey,
      orderstatusid = orderstatusid,
      forclient = forclient)
  }

  def create(validator: JOrderStatusMessageNew): Option[OrderStatusMessage] = {
    val data = for {
      os <- OrderStatus.find(validator.orderstatusid)
      lan <- Language.getDefault
    } yield (os, lan)
    data.map { case (ordSt, lang) =>
      val newMessage = create(
        ordSt.id, validator.forclient
      )
      OrderStatusMessageText.create(
        messageid = newMessage.id,
        languageid = lang.id,
        messagetitle = validator.messagetitle,
        messagetext = validator.messagetext
      )
      Some(newMessage)
    }.getOrElse(None)
  }


  def save(entity: OrderStatusMessage)(implicit session: DBSession = autoSession): OrderStatusMessage = {
    withSQL {
      update(OrderStatusMessage).set(
        column.id -> entity.id,
        column.orderstatusid -> entity.orderstatusid,
        column.forclient -> entity.forclient
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }


  def save(id: Long, validator: JOrderStatusMessageUpdate): Either[Exception, OrderStatusMessage] = {
    val data = for {
      mess <- OrderStatusMessage.find(id)
      lang <- Language.getByCod(validator.languagecod)
    } yield (mess, lang)
    data.map { case (mess, lang) =>
      val newText: Option[OrderStatusMessageText] = OrderStatusMessageText.find(messageid = mess.id, languageid = lang.id).map { t =>
        Some(t.copy(
          messagetitle = validator.messagetitle.getOrElse(t.messagetitle),
          messagetext = validator.messagetext.getOrElse(t.messagetext))
          .save())
      }.getOrElse {
        val texts = for {
          title <- validator.messagetitle
          text <- validator.messagetext
        } yield (title, text)
        val newTxt: Option[OrderStatusMessageText] = texts.map { case (title, text) =>
          OrderStatusMessageText.create(
            messageid = mess.id,
            languageid = lang.id,
            messagetitle = title,
            messagetext = text
          )
        }
        newTxt
      }
      newText.map{t=>
        Right(mess.copy(orderstatusid = validator.orderstatusid, forclient = validator.forclient).save())
      }.getOrElse(Left(new IllegalArgumentException))
    }.getOrElse(Left(new NoSuchElementException))
  }

  def destroy(entity: OrderStatusMessage)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(OrderStatusMessage).where.eq(column.id, entity.id)
    }.update.apply()
  }

}
