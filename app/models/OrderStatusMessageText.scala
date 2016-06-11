package models

import scalikejdbc._

case class OrderStatusMessageText(
  messageid: Long,
  languageid: Long,
  messagetitle: String,
  messagetext: String) {

  def save()(implicit session: DBSession = OrderStatusMessageText.autoSession): OrderStatusMessageText = OrderStatusMessageText.save(this)(session)

  def destroy()(implicit session: DBSession = OrderStatusMessageText.autoSession): Unit = OrderStatusMessageText.destroy(this)(session)

}


object OrderStatusMessageText extends SQLSyntaxSupport[OrderStatusMessageText] {

  override val tableName = "order_status_message_text"

  override val columns = Seq("messageid", "languageid", "messagetitle", "messagetext")

  def apply(osmt: SyntaxProvider[OrderStatusMessageText])(rs: WrappedResultSet): OrderStatusMessageText = apply(osmt.resultName)(rs)
  def apply(osmt: ResultName[OrderStatusMessageText])(rs: WrappedResultSet): OrderStatusMessageText = new OrderStatusMessageText(
    messageid = rs.get(osmt.messageid),
    languageid = rs.get(osmt.languageid),
    messagetitle = rs.get(osmt.messagetitle),
    messagetext = rs.get(osmt.messagetext)
  )

  val osmt = OrderStatusMessageText.syntax("osmt")

  override val autoSession = AutoSession

  def find(messageid: Long, languageid: Long)(implicit session: DBSession = autoSession): Option[OrderStatusMessageText] = {
    withSQL {
      select.from(OrderStatusMessageText as osmt).where.eq(osmt.messageid, messageid).and.eq(osmt.languageid, languageid)
    }.map(OrderStatusMessageText(osmt.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[OrderStatusMessageText] = {
    withSQL(select.from(OrderStatusMessageText as osmt)).map(OrderStatusMessageText(osmt.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(OrderStatusMessageText as osmt)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[OrderStatusMessageText] = {
    withSQL {
      select.from(OrderStatusMessageText as osmt).where.append(where)
    }.map(OrderStatusMessageText(osmt.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[OrderStatusMessageText] = {
    withSQL {
      select.from(OrderStatusMessageText as osmt).where.append(where)
    }.map(OrderStatusMessageText(osmt.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(OrderStatusMessageText as osmt).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    messageid: Long,
    languageid: Long,
    messagetitle: String,
    messagetext: String)(implicit session: DBSession = autoSession): OrderStatusMessageText = {
    withSQL {
      insert.into(OrderStatusMessageText).columns(
        column.messageid,
        column.languageid,
        column.messagetitle,
        column.messagetext
      ).values(
        messageid,
        languageid,
        messagetitle,
        messagetext
      )
    }.update.apply()

    OrderStatusMessageText(
      messageid = messageid,
      languageid = languageid,
      messagetitle = messagetitle,
      messagetext = messagetext)
  }

  def save(entity: OrderStatusMessageText)(implicit session: DBSession = autoSession): OrderStatusMessageText = {
    withSQL {
      update(OrderStatusMessageText).set(
        column.messageid -> entity.messageid,
        column.languageid -> entity.languageid,
        column.messagetitle -> entity.messagetitle,
        column.messagetext -> entity.messagetext
      ).where.eq(column.messageid, entity.messageid).and.eq(column.languageid, entity.languageid)
    }.update.apply()
    entity
  }

  def destroy(entity: OrderStatusMessageText)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(OrderStatusMessageText).where.eq(column.messageid, entity.messageid).and.eq(column.languageid, entity.languageid) }.update.apply()
  }

}
