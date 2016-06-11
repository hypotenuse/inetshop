package models

import scalikejdbc._

case class OrderStatusText(
  orderstatusid: Long,
  languageid: Long,
  title: String) {

  def save()(implicit session: DBSession = OrderStatusText.autoSession): OrderStatusText = OrderStatusText.save(this)(session)

  def destroy()(implicit session: DBSession = OrderStatusText.autoSession): Unit = OrderStatusText.destroy(this)(session)

}


object OrderStatusText extends SQLSyntaxSupport[OrderStatusText] {

  override val tableName = "order_status_texts"

  override val columns = Seq("orderstatusid", "languageid", "title")

  def apply(ost: SyntaxProvider[OrderStatusText])(rs: WrappedResultSet): OrderStatusText = apply(ost.resultName)(rs)
  def apply(ost: ResultName[OrderStatusText])(rs: WrappedResultSet): OrderStatusText = new OrderStatusText(
    orderstatusid = rs.get(ost.orderstatusid),
    languageid = rs.get(ost.languageid),
    title = rs.get(ost.title)
  )

  val ost = OrderStatusText.syntax("ost")

  override val autoSession = AutoSession

  def find(orderstatusid: Long, languageid: Long)(implicit session: DBSession = autoSession): Option[OrderStatusText] = {
    withSQL {
      select.from(OrderStatusText as ost).where.eq(ost.orderstatusid, orderstatusid).and.eq(ost.languageid, languageid)
    }.map(OrderStatusText(ost.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[OrderStatusText] = {
    withSQL(select.from(OrderStatusText as ost)).map(OrderStatusText(ost.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(OrderStatusText as ost)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[OrderStatusText] = {
    withSQL {
      select.from(OrderStatusText as ost).where.append(where)
    }.map(OrderStatusText(ost.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[OrderStatusText] = {
    withSQL {
      select.from(OrderStatusText as ost).where.append(where)
    }.map(OrderStatusText(ost.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(OrderStatusText as ost).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    orderstatusid: Long,
    languageid: Long,
    title: String)(implicit session: DBSession = autoSession): OrderStatusText = {
    withSQL {
      insert.into(OrderStatusText).columns(
        column.orderstatusid,
        column.languageid,
        column.title
      ).values(
        orderstatusid,
        languageid,
        title
      )
    }.update.apply()

    OrderStatusText(
      orderstatusid = orderstatusid,
      languageid = languageid,
      title = title)
  }

  def save(entity: OrderStatusText)(implicit session: DBSession = autoSession): OrderStatusText = {
    withSQL {
      update(OrderStatusText).set(
        column.orderstatusid -> entity.orderstatusid,
        column.languageid -> entity.languageid,
        column.title -> entity.title
      ).where.eq(column.orderstatusid, entity.orderstatusid).and.eq(column.languageid, entity.languageid)
    }.update.apply()
    entity
  }

  def destroy(entity: OrderStatusText)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(OrderStatusText).where.eq(column.orderstatusid, entity.orderstatusid).and.eq(column.languageid, entity.languageid) }.update.apply()
  }

}
