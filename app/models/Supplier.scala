package models

import java.util.NoSuchElementException

import scalikejdbc._
import validators.{JSupplierCurrency, JSupplierUpdate, JSupplierNew}

case class Supplier(
                     id: Long,
                     title: String,
                     info: Option[String] = None) {

  def save()(implicit session: DBSession = Supplier.autoSession): Supplier = Supplier.save(this)(session)

  def destroy()(implicit session: DBSession = Supplier.autoSession): Unit = Supplier.destroy(this)(session)

}


object Supplier extends SQLSyntaxSupport[Supplier] {

  override val tableName = "suppliers"

  override val columns = Seq("id", "title", "info")

  def apply(s: SyntaxProvider[Supplier])(rs: WrappedResultSet): Supplier = apply(s.resultName)(rs)

  def apply(s: ResultName[Supplier])(rs: WrappedResultSet): Supplier = new Supplier(
    id = rs.get(s.id),
    title = rs.get(s.title),
    info = rs.get(s.info)
  )

  val s = Supplier.syntax("s")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Supplier] = {
    withSQL {
      select.from(Supplier as s).where.eq(s.id, id)
    }.map(Supplier(s.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Supplier] = {
    withSQL(select.from(Supplier as s)).map(Supplier(s.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Supplier as s)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Supplier] = {
    withSQL {
      select.from(Supplier as s).where.append(where)
    }.map(Supplier(s.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Supplier] = {
    withSQL {
      select.from(Supplier as s).where.append(where)
    }.map(Supplier(s.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Supplier as s).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              title: String,
              info: Option[String] = None)(implicit session: DBSession = autoSession): Supplier = {
    val generatedKey = withSQL {
      insert.into(Supplier).columns(
        column.title,
        column.info
      ).values(
        title,
        info
      )
    }.updateAndReturnGeneratedKey.apply()

    Supplier(
      id = generatedKey,
      title = title,
      info = info)
  }

  def create(jSupplierNew: JSupplierNew): Supplier = {
    create(
      title = jSupplierNew.title
    )
  }

  def save(entity: Supplier)(implicit session: DBSession = autoSession): Supplier = {
    withSQL {
      update(Supplier).set(
        column.id -> entity.id,
        column.title -> entity.title,
        column.info -> entity.info
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(id: Long, jSupplierUpdate: JSupplierUpdate) = {
    find(id).map { s =>
      s.copy(
        title = jSupplierUpdate.title.getOrElse(s.title),
        info = jSupplierUpdate.info.orElse(s.info)
      ).save()

      jSupplierUpdate.currencies.foreach { list =>

        val currencyData: List[(Long, BigDecimal)] = for {
          c: JSupplierCurrency <- list
          rate <- c.rate
        } yield (c.id, rate)

        currencyData.foreach{case(currencyId, rate) =>
          SupplierCurrencyRate.find(supplier = s.id, currency = currencyId).map{cr =>
            cr.copy(rate = rate).save()
          }.getOrElse(SupplierCurrencyRate.create(supplier = s.id, currency = currencyId, rate = rate))
        }
      }



    }.getOrElse(throw new NoSuchElementException)
  }

  def destroy(entity: Supplier)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(Supplier).where.eq(column.id, entity.id)
    }.update.apply()
  }

}
