package models

import java.security.MessageDigest

import org.apache.commons.codec.digest.DigestUtils
import play.api.libs.json.Json
import scalikejdbc._
import org.joda.time.{DateTime}

case class RawSupplierGood(
  id: Long,
  supplier: Long,
  title: String,
  cost: BigDecimal,
  costCurrencyId: Long,
  updated: DateTime,
  costRrzCurrencyId: Option[Long] = None,
  costRrz: Option[BigDecimal] = None,
  partnumber: Option[String] = None,
  warranty: Option[Int] = None,
  brand: Option[String] = None,
  supplierGoodid: Option[String] = None,
  category: Option[String] = None,
  description: Option[String] = None,
  imageUrl: Option[String] = None,
  model: Option[String] = None,
                            text: String ) {

  def save()(implicit session: DBSession = RawSupplierGood.autoSession): RawSupplierGood = RawSupplierGood.save(this)(session)

  def destroy()(implicit session: DBSession = RawSupplierGood.autoSession): Unit = RawSupplierGood.destroy(this)(session)

}


object RawSupplierGood extends SQLSyntaxSupport[RawSupplierGood] {

  override val tableName = "raw_suppliers_goods"

  override val columns = Seq("id", "supplier", "title", "cost", "cost_currency_id", "updated", "cost_rrz_currency_id", "cost_rrz", "partnumber", "warranty", "brand", "supplier_goodid", "category", "description", "image_url", "model", "text")

  def apply(rsg: SyntaxProvider[RawSupplierGood])(rs: WrappedResultSet): RawSupplierGood = apply(rsg.resultName)(rs)
  def apply(rsg: ResultName[RawSupplierGood])(rs: WrappedResultSet): RawSupplierGood = new RawSupplierGood(
    id = rs.get(rsg.id),
    supplier = rs.get(rsg.supplier),
    title = rs.get(rsg.title),
    cost = rs.get(rsg.cost),
    costCurrencyId = rs.get(rsg.costCurrencyId),
    updated = rs.get(rsg.updated),
    costRrzCurrencyId = rs.get(rsg.costRrzCurrencyId),
    costRrz = rs.get(rsg.costRrz),
    partnumber = rs.get(rsg.partnumber),
    warranty = rs.get(rsg.warranty),
    brand = rs.get(rsg.brand),
    supplierGoodid = rs.get(rsg.supplierGoodid),
    category = rs.get(rsg.category),
    description = rs.get(rsg.description),
    imageUrl = rs.get(rsg.imageUrl),
    model = rs.get(rsg.model),
    text = rs.get(rsg.text)
  )

  val rsg = RawSupplierGood.syntax("rsg")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[RawSupplierGood] = {
    withSQL {
      select.from(RawSupplierGood as rsg).where.eq(rsg.id, id)
    }.map(RawSupplierGood(rsg.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[RawSupplierGood] = {
    withSQL(select.from(RawSupplierGood as rsg)).map(RawSupplierGood(rsg.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(RawSupplierGood as rsg)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[RawSupplierGood] = {
    withSQL {
      select.from(RawSupplierGood as rsg).where.append(where)
    }.map(RawSupplierGood(rsg.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[RawSupplierGood] = {
    withSQL {
      select.from(RawSupplierGood as rsg).where.append(where)
    }.map(RawSupplierGood(rsg.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(RawSupplierGood as rsg).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    supplier: Long,
    title: String,
    cost: BigDecimal,
    costCurrencyId: Long,
    costRrzCurrencyId: Option[Long] = None,
    costRrz: Option[BigDecimal] = None,
    partnumber: Option[String] = None,
    warranty: Option[Int] = None,
    brand: Option[String] = None,
    supplierGoodid: Option[String] = None,
    category: Option[String] = None,
    description: Option[String] = None,
    imageUrl: Option[String] = None,
    model: Option[String] = None)(implicit session: DBSession = autoSession): RawSupplierGood = {
    val updated = DateTime.now()
    val text = createText(
      title = title,
      partnumber = partnumber,
      supplierGoodid = supplierGoodid,
      description = description,
      model = model
    )
    val generatedKey = withSQL {
      insert.into(RawSupplierGood).columns(
        column.supplier,
        column.title,
        column.cost,
        column.costCurrencyId,
        column.updated,
        column.costRrzCurrencyId,
        column.costRrz,
        column.partnumber,
        column.warranty,
        column.brand,
        column.supplierGoodid,
        column.category,
        column.description,
        column.imageUrl,
        column.model,
        column.text
      ).values(
        supplier,
        title,
        cost,
        costCurrencyId,
        updated,
        costRrzCurrencyId,
        costRrz,
        partnumber,
        warranty,
        brand,
        supplierGoodid,
        category,
        description,
        imageUrl,
        model,
        text
      )
    }.updateAndReturnGeneratedKey.apply()

    RawSupplierGood(
      id = generatedKey,
      supplier = supplier,
      title = title,
      cost = cost,
      costCurrencyId = costCurrencyId,
      updated = updated,
      costRrzCurrencyId = costRrzCurrencyId,
      costRrz = costRrz,
      partnumber = partnumber,
      warranty = warranty,
      brand = brand,
      supplierGoodid = supplierGoodid,
      category = category,
      description = description,
      imageUrl = imageUrl,
      model = model,
    text = text
    )
  }

  def save(entity: RawSupplierGood)(implicit session: DBSession = autoSession): RawSupplierGood = {
    val text = createText(
      title = entity.title,
      partnumber = entity.partnumber,
      supplierGoodid = entity.supplierGoodid,
      description = entity.description,
      model = entity.model
    )
    withSQL {
      update(RawSupplierGood).set(
        column.id -> entity.id,
        column.supplier -> entity.supplier,
        column.title -> entity.title,
        column.cost -> entity.cost,
        column.costCurrencyId -> entity.costCurrencyId,
        column.updated -> entity.updated,
        column.costRrzCurrencyId -> entity.costRrzCurrencyId,
        column.costRrz -> entity.costRrz,
        column.partnumber -> entity.partnumber,
        column.warranty -> entity.warranty,
        column.brand -> entity.brand,
        column.supplierGoodid -> entity.supplierGoodid,
        column.category -> entity.category,
        column.description -> entity.description,
        column.imageUrl -> entity.imageUrl,
        column.model -> entity.model,
        column.text -> text
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def createText(    title: String,
                     partnumber: Option[String] = None,
                     supplierGoodid: Option[String] = None,
                     description: Option[String] = None,
                     model: Option[String] = None): String = {
    val part = partnumber.map(p => p.replaceAll("[\\s]", "")).getOrElse("")
    val mod = model.map(m => m.replaceAll("[\\s]", "")).getOrElse("")
    val desc = description.map(d => d.replaceAll("[\\s]", "")).getOrElse("")
    val supplGoodId = supplierGoodid.map(id => id.replaceAll("[\\s]", "")).getOrElse("")
    val titl = title.replaceAll("[\\s]", "")
    val total = titl + part + mod + desc + supplGoodId
    DigestUtils.md5Hex(total)
  }

  def destroy(entity: RawSupplierGood)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(RawSupplierGood).where.eq(column.id, entity.id) }.update.apply()
  }

}
