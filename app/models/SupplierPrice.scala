package models

import scalikejdbc._
import org.joda.time.{DateTime}

case class SupplierPrice(
  updated: DateTime,
  good: Long,
  supplier: Long,
  cost: BigDecimal,
  currency: Long,
  costRrzCurrencyId: Option[Long] = None,
  costRrz: Option[BigDecimal] = None) {

  def save()(implicit session: DBSession = SupplierPrice.autoSession): SupplierPrice = SupplierPrice.save(this)(session)

  def destroy()(implicit session: DBSession = SupplierPrice.autoSession): Unit = SupplierPrice.destroy(this)(session)

}


object SupplierPrice extends SQLSyntaxSupport[SupplierPrice] {

  override val tableName = "suppliers_prices"

  override val columns = Seq("updated", "good", "supplier", "cost", "currency", "cost_rrz_currency_id", "cost_rrz")

  def apply(sp: SyntaxProvider[SupplierPrice])(rs: WrappedResultSet): SupplierPrice = apply(sp.resultName)(rs)
  def apply(sp: ResultName[SupplierPrice])(rs: WrappedResultSet): SupplierPrice = new SupplierPrice(
    updated = rs.get(sp.updated),
    good = rs.get(sp.good),
    supplier = rs.get(sp.supplier),
    cost = rs.get(sp.cost),
    currency = rs.get(sp.currency),
    costRrzCurrencyId = rs.get(sp.costRrzCurrencyId),
    costRrz = rs.get(sp.costRrz)
  )

  val sp = SupplierPrice.syntax("sp")

  override val autoSession = AutoSession

  def find(good: Long, supplier: Long)(implicit session: DBSession = autoSession): Option[SupplierPrice] = {
    withSQL {
      select.from(SupplierPrice as sp).where.eq(sp.good, good).and.eq(sp.supplier, supplier)
    }.map(SupplierPrice(sp.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[SupplierPrice] = {
    withSQL(select.from(SupplierPrice as sp)).map(SupplierPrice(sp.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(SupplierPrice as sp)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[SupplierPrice] = {
    withSQL {
      select.from(SupplierPrice as sp).where.append(where)
    }.map(SupplierPrice(sp.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[SupplierPrice] = {
    withSQL {
      select.from(SupplierPrice as sp).where.append(where)
    }.map(SupplierPrice(sp.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(SupplierPrice as sp).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    updated: DateTime,
    good: Long,
    supplier: Long,
    cost: BigDecimal,
    currency: Long,
    costRrzCurrencyId: Option[Long] = None,
    costRrz: Option[BigDecimal] = None)(implicit session: DBSession = autoSession): SupplierPrice = {
    withSQL {
      insert.into(SupplierPrice).columns(
        column.updated,
        column.good,
        column.supplier,
        column.cost,
        column.currency,
        column.costRrzCurrencyId,
        column.costRrz
      ).values(
        updated,
        good,
        supplier,
        cost,
        currency,
        costRrzCurrencyId,
        costRrz
      )
    }.update.apply()

    SupplierPrice(
      updated = updated,
      good = good,
      supplier = supplier,
      cost = cost,
      currency = currency,
      costRrzCurrencyId = costRrzCurrencyId,
      costRrz = costRrz)
  }

  def save(entity: SupplierPrice)(implicit session: DBSession = autoSession): SupplierPrice = {
    withSQL {
      update(SupplierPrice).set(
        column.updated -> entity.updated,
        column.good -> entity.good,
        column.supplier -> entity.supplier,
        column.cost -> entity.cost,
        column.currency -> entity.currency,
        column.costRrzCurrencyId -> entity.costRrzCurrencyId,
        column.costRrz -> entity.costRrz
      ).where.eq(column.good, entity.good).and.eq(column.supplier, entity.supplier)
    }.update.apply()
    entity
  }

  def destroy(entity: SupplierPrice)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(SupplierPrice).where.eq(column.good, entity.good).and.eq(column.supplier, entity.supplier) }.update.apply()
  }

}
