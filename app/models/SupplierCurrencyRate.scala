package models

import scalikejdbc._

case class SupplierCurrencyRate(
  supplier: Long,
  currency: Long,
  rate: BigDecimal) {

  def save()(implicit session: DBSession = SupplierCurrencyRate.autoSession): SupplierCurrencyRate = SupplierCurrencyRate.save(this)(session)

  def destroy()(implicit session: DBSession = SupplierCurrencyRate.autoSession): Unit = SupplierCurrencyRate.destroy(this)(session)

}


object SupplierCurrencyRate extends SQLSyntaxSupport[SupplierCurrencyRate] {

  override val tableName = "suppliers_curr_exch_rate"

  override val columns = Seq("supplier", "currency", "rate")

  def apply(scr: SyntaxProvider[SupplierCurrencyRate])(rs: WrappedResultSet): SupplierCurrencyRate = apply(scr.resultName)(rs)
  def apply(scr: ResultName[SupplierCurrencyRate])(rs: WrappedResultSet): SupplierCurrencyRate = new SupplierCurrencyRate(
    supplier = rs.get(scr.supplier),
    currency = rs.get(scr.currency),
    rate = rs.get(scr.rate)
  )

  val scr = SupplierCurrencyRate.syntax("scr")

  override val autoSession = AutoSession

  def find(supplier: Long, currency: Long)(implicit session: DBSession = autoSession): Option[SupplierCurrencyRate] = {
    withSQL {
      select.from(SupplierCurrencyRate as scr).where.eq(scr.supplier, supplier).and.eq(scr.currency, currency)
    }.map(SupplierCurrencyRate(scr.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[SupplierCurrencyRate] = {
    withSQL(select.from(SupplierCurrencyRate as scr)).map(SupplierCurrencyRate(scr.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(SupplierCurrencyRate as scr)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[SupplierCurrencyRate] = {
    withSQL {
      select.from(SupplierCurrencyRate as scr).where.append(where)
    }.map(SupplierCurrencyRate(scr.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[SupplierCurrencyRate] = {
    withSQL {
      select.from(SupplierCurrencyRate as scr).where.append(where)
    }.map(SupplierCurrencyRate(scr.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(SupplierCurrencyRate as scr).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    supplier: Long,
    currency: Long,
    rate: BigDecimal)(implicit session: DBSession = autoSession): SupplierCurrencyRate = {
    withSQL {
      insert.into(SupplierCurrencyRate).columns(
        column.supplier,
        column.currency,
        column.rate
      ).values(
        supplier,
        currency,
        rate
      )
    }.update.apply()

    SupplierCurrencyRate(
      supplier = supplier,
      currency = currency,
      rate = rate)
  }

  def save(entity: SupplierCurrencyRate)(implicit session: DBSession = autoSession): SupplierCurrencyRate = {
    withSQL {
      update(SupplierCurrencyRate).set(
        column.supplier -> entity.supplier,
        column.currency -> entity.currency,
        column.rate -> entity.rate
      ).where.eq(column.supplier, entity.supplier).and.eq(column.currency, entity.currency)
    }.update.apply()
    entity
  }

  def destroy(entity: SupplierCurrencyRate)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(SupplierCurrencyRate).where.eq(column.supplier, entity.supplier).and.eq(column.currency, entity.currency) }.update.apply()
  }

}
