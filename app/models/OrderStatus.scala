package models

import scalikejdbc._
import validators.JOrderStatusUpdate

case class OrderStatus(
                        id: Long,
                        sendmessageClient: Boolean,
                        sendmessageAdmin: Boolean) {

  def save()(implicit session: DBSession = OrderStatus.autoSession): OrderStatus = OrderStatus.save(this)(session)

  def destroy()(implicit session: DBSession = OrderStatus.autoSession): Unit = OrderStatus.destroy(this)(session)
  def textByDefaultLang(implicit session: DBSession = OrderStatus.autoSession): Option[OrderStatusText] = OrderStatus.getTextByDefaultLang(this)(session)
}


object OrderStatus extends SQLSyntaxSupport[OrderStatus] {

  override val tableName = "order_statuses"

  override val columns = Seq("id", "sendmessage_client", "sendmessage_admin")

  def apply(os: SyntaxProvider[OrderStatus])(rs: WrappedResultSet): OrderStatus = apply(os.resultName)(rs)

  def apply(os: ResultName[OrderStatus])(rs: WrappedResultSet): OrderStatus = new OrderStatus(
    id = rs.get(os.id),
    sendmessageClient = rs.get(os.sendmessageClient),
    sendmessageAdmin = rs.get(os.sendmessageAdmin)
  )

  val os = OrderStatus.syntax("os")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[OrderStatus] = {
    withSQL {
      select.from(OrderStatus as os).where.eq(os.id, id)
    }.map(OrderStatus(os.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[OrderStatus] = {
    withSQL(select.from(OrderStatus as os)).map(OrderStatus(os.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(OrderStatus as os)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[OrderStatus] = {
    withSQL {
      select.from(OrderStatus as os).where.append(where)
    }.map(OrderStatus(os.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[OrderStatus] = {
    withSQL {
      select.from(OrderStatus as os).where.append(where)
    }.map(OrderStatus(os.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(OrderStatus as os).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              sendmessageClient: Boolean = false,
              sendmessageAdmin: Boolean = false)(implicit session: DBSession = autoSession): OrderStatus = {
    val generatedKey = withSQL {
      insert.into(OrderStatus).columns(
        column.sendmessageClient,
        column.sendmessageAdmin
      ).values(
        sendmessageClient,
        sendmessageAdmin
      )
    }.updateAndReturnGeneratedKey.apply()

    OrderStatus(
      id = generatedKey,
      sendmessageClient = sendmessageClient,
      sendmessageAdmin = sendmessageAdmin)
  }

  def create(validator: validators.JOrderStatusNew): OrderStatus = {
    Language.getDefault.map { l =>
      val newStatus = create()
      OrderStatusText.create(newStatus.id, l.id, validator.title)
      newStatus
    } getOrElse (throw new Exception("Default language not found"))

  }

  def save(entity: OrderStatus)(implicit session: DBSession = autoSession): OrderStatus = {
    withSQL {
      update(OrderStatus).set(
        column.id -> entity.id,
        column.sendmessageClient -> entity.sendmessageClient,
        column.sendmessageAdmin -> entity.sendmessageAdmin
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(orderStatusId: Long, jOrderStatus: JOrderStatusUpdate): OrderStatus = {
    val maybeOrderStatus = OrderStatus.find(orderStatusId)
    val lang = Language.getByCod(jOrderStatus.languagecod).getOrElse {
      throw new IllegalArgumentException("Can't find language with code ${jOrderStatus.languagecod} in database!")
    }

    maybeOrderStatus.map {
      status =>
        OrderStatusText.find(languageid = lang.id, orderstatusid = status.id).map {
          t =>
            t.copy(
              title = jOrderStatus.title.getOrElse(t.title)
            ).save()

        }.getOrElse {
          jOrderStatus.title.map{t=>
            OrderStatusText.create(
              orderstatusid = status.id,
              languageid = lang.id,
              title = t
            )
          }.getOrElse(throw new IllegalArgumentException(s"Can't create text for language ${lang.cod} with empty title"))

        }
        status.copy(sendmessageClient = jOrderStatus.sendmessageClient, sendmessageAdmin = jOrderStatus.sendmessageAdmin).save()

    }.getOrElse(throw new NoSuchElementException("Order status does not exist"))

  }

  def destroy(entity: OrderStatus)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(OrderStatus).where.eq(column.id, entity.id)
    }.update.apply()
  }

  def getTextByDefaultLang(entity: OrderStatus)(implicit session: DBSession = autoSession): Option[OrderStatusText]={
    for{
      lan <- Language.findBy(sqls"defaultlng = TRUE")
      text <- OrderStatusText.find(languageid=lan.id, orderstatusid=entity.id)
    } yield text
  }

}
