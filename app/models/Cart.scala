package models

import scalikejdbc._
import org.joda.time.{DateTime}

case class Cart(
  id: Long,
  customerid: Option[Long] = None,
  amount: Long,
  created: Option[DateTime] = None) {

  def save()(implicit session: DBSession = Cart.autoSession): Cart = Cart.save(this)(session)

  def destroy()(implicit session: DBSession = Cart.autoSession): Unit = Cart.destroy(this)(session)

}


object Cart extends SQLSyntaxSupport[Cart] {

  override val tableName = "carts"

  override val columns = Seq("id", "customerid", "amount", "created")

  def apply(c: SyntaxProvider[Cart])(rs: WrappedResultSet): Cart = apply(c.resultName)(rs)
  def apply(c: ResultName[Cart])(rs: WrappedResultSet): Cart = new Cart(
    id = rs.get(c.id),
    customerid = rs.get(c.customerid),
    amount = rs.get(c.amount),
    created = rs.get(c.created)
  )

  val c = Cart.syntax("c")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Cart] = {
    withSQL {
      select.from(Cart as c).where.eq(c.id, id)
    }.map(Cart(c.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Cart] = {
    withSQL(select.from(Cart as c)).map(Cart(c.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Cart as c)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Cart] = {
    withSQL {
      select.from(Cart as c).where.append(where)
    }.map(Cart(c.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Cart] = {
    withSQL {
      select.from(Cart as c).where.append(where)
    }.map(Cart(c.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Cart as c).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    customerid: Option[Long] = None,
    amount: Long,
    created: Option[DateTime] = None)(implicit session: DBSession = autoSession): Cart = {
    val generatedKey = withSQL {
      insert.into(Cart).columns(
        column.customerid,
        column.amount,
        column.created
      ).values(
        customerid,
        amount,
        created
      )
    }.updateAndReturnGeneratedKey.apply()

    Cart(
      id = generatedKey,
      customerid = customerid,
      amount = amount,
      created = created)
  }

  def save(entity: Cart)(implicit session: DBSession = autoSession): Cart = {
    withSQL {
      update(Cart).set(
        column.id -> entity.id,
        column.customerid -> entity.customerid,
        column.amount -> entity.amount,
        column.created -> entity.created
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Cart)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Cart).where.eq(column.id, entity.id) }.update.apply()
  }

}
