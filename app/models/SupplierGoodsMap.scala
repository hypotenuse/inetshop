package models

import scalikejdbc._

case class SupplierGoodsMap(
  text: String,
  good: Long,
  supplier: Long) {

  def save()(implicit session: DBSession = SupplierGoodsMap.autoSession): SupplierGoodsMap = SupplierGoodsMap.save(this)(session)

  def destroy()(implicit session: DBSession = SupplierGoodsMap.autoSession): Unit = SupplierGoodsMap.destroy(this)(session)

}


object SupplierGoodsMap extends SQLSyntaxSupport[SupplierGoodsMap] {

  override val tableName = "suppliers_goods_map"

  override val columns = Seq("text", "good", "supplier")

  def apply(sgm: SyntaxProvider[SupplierGoodsMap])(rs: WrappedResultSet): SupplierGoodsMap = apply(sgm.resultName)(rs)
  def apply(sgm: ResultName[SupplierGoodsMap])(rs: WrappedResultSet): SupplierGoodsMap = new SupplierGoodsMap(
    text = rs.get(sgm.text),
    good = rs.get(sgm.good),
    supplier = rs.get(sgm.supplier)
  )

  val sgm = SupplierGoodsMap.syntax("sgm")

  override val autoSession = AutoSession

  def find(good: Long, supplier: Long, text: String)(implicit session: DBSession = autoSession): Option[SupplierGoodsMap] = {
    withSQL {
      select.from(SupplierGoodsMap as sgm).where.eq(sgm.good, good).and.eq(sgm.supplier, supplier).and.eq(sgm.text, text)
    }.map(SupplierGoodsMap(sgm.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[SupplierGoodsMap] = {
    withSQL(select.from(SupplierGoodsMap as sgm)).map(SupplierGoodsMap(sgm.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(SupplierGoodsMap as sgm)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[SupplierGoodsMap] = {
    withSQL {
      select.from(SupplierGoodsMap as sgm).where.append(where)
    }.map(SupplierGoodsMap(sgm.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[SupplierGoodsMap] = {
    withSQL {
      select.from(SupplierGoodsMap as sgm).where.append(where)
    }.map(SupplierGoodsMap(sgm.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(SupplierGoodsMap as sgm).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    text: String,
    good: Long,
    supplier: Long)(implicit session: DBSession = autoSession): SupplierGoodsMap = {
    withSQL {
      insert.into(SupplierGoodsMap).columns(
        column.text,
        column.good,
        column.supplier
      ).values(
        text,
        good,
        supplier
      )
    }.update.apply()

    SupplierGoodsMap(
      text = text,
      good = good,
      supplier = supplier)
  }

  def save(entity: SupplierGoodsMap)(implicit session: DBSession = autoSession): SupplierGoodsMap = {
    withSQL {
      update(SupplierGoodsMap).set(
        column.text -> entity.text,
        column.good -> entity.good,
        column.supplier -> entity.supplier
      ).where.eq(column.good, entity.good).and.eq(column.supplier, entity.supplier).and.eq(column.text, entity.text)
    }.update.apply()
    entity
  }

  def destroy(entity: SupplierGoodsMap)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(SupplierGoodsMap).where.eq(column.good, entity.good).and.eq(column.supplier, entity.supplier).and.eq(column.text, entity.text) }.update.apply()
  }

}
