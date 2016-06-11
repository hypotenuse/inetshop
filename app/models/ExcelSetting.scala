package models

import scalikejdbc._
import validators.{JExcelSetting, CurrencyIdSign, CategoryColumn}


case class ExcelSetting(
  id: Long,
  supplier: Long,
  titleColumn: Int,
  sheetNumber: Int,
  title: String,
  costColumn: Int,
  costCurrencyId: Option[Long] = None,
  costRrzColumn: Option[Int] = None,
  partnumberColumn: Option[Int] = None,
  warrantyColumn: Option[Int] = None,
  brandColumn: Option[Int] = None,
  goodidSupplierColumn: Option[Int] = None,
  categoryColumns: Option[String] = None,
  descriptionColumn: Option[Int] = None,
  imageUrlColumn: Option[Int] = None,
  warrantyInMonths: Option[Boolean] = None,
  modelColumn: Option[Int] = None,
  amountColumn: Int,
  currencySignColumn: Option[Int] = None,
  currencyIdSign: Option[String] = None,
  costRrzCurrencyId: Option[Long] = None) {

  def save()(implicit session: DBSession = ExcelSetting.autoSession): ExcelSetting = ExcelSetting.save(this)(session)

  def destroy()(implicit session: DBSession = ExcelSetting.autoSession): Unit = ExcelSetting.destroy(this)(session)

}


object ExcelSetting extends SQLSyntaxSupport[ExcelSetting] {

  override val tableName = "suppliers_excel_settings"

  override val columns = Seq("id", "supplier", "title_column", "sheet_number", "title", "cost_column", "cost_currency_id", "cost_rrz_column", "partnumber_column", "warranty_column", "brand_column", "goodid_supplier_column", "category_columns", "description_column", "image_url_column", "warranty_in_months", "model_column", "amount_column", "currency_sign_column", "currency_id_sign", "cost_rrz_currency_id")

  def apply(es: SyntaxProvider[ExcelSetting])(rs: WrappedResultSet): ExcelSetting = apply(es.resultName)(rs)
  def apply(es: ResultName[ExcelSetting])(rs: WrappedResultSet): ExcelSetting = new ExcelSetting(
    id = rs.get(es.id),
    supplier = rs.get(es.supplier),
    titleColumn = rs.get(es.titleColumn),
    sheetNumber = rs.get(es.sheetNumber),
    title = rs.get(es.title),
    costColumn = rs.get(es.costColumn),
    costCurrencyId = rs.get(es.costCurrencyId),
    costRrzColumn = rs.get(es.costRrzColumn),
    partnumberColumn = rs.get(es.partnumberColumn),
    warrantyColumn = rs.get(es.warrantyColumn),
    brandColumn = rs.get(es.brandColumn),
    goodidSupplierColumn = rs.get(es.goodidSupplierColumn),
    categoryColumns = rs.get(es.categoryColumns),
    descriptionColumn = rs.get(es.descriptionColumn),
    imageUrlColumn = rs.get(es.imageUrlColumn),
    warrantyInMonths = rs.get(es.warrantyInMonths),
    modelColumn = rs.get(es.modelColumn),
    amountColumn = rs.get(es.amountColumn),
    currencySignColumn = rs.get(es.currencySignColumn),
    costRrzCurrencyId = rs.get(es.costRrzCurrencyId),
    currencyIdSign = rs.get(es.currencyIdSign)
  )

  val es = ExcelSetting.syntax("es")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[ExcelSetting] = {
    withSQL {
      select.from(ExcelSetting as es).where.eq(es.id, id)
    }.map(ExcelSetting(es.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ExcelSetting] = {
    withSQL(select.from(ExcelSetting as es)).map(ExcelSetting(es.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(ExcelSetting as es)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ExcelSetting] = {
    withSQL {
      select.from(ExcelSetting as es).where.append(where)
    }.map(ExcelSetting(es.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ExcelSetting] = {
    withSQL {
      select.from(ExcelSetting as es).where.append(where)
    }.map(ExcelSetting(es.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(ExcelSetting as es).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    supplier: Long,
    titleColumn: Int,
    sheetNumber: Int,
    title: String,
    costColumn: Int,
    costCurrencyId: Option[Long] = None,
    costRrzColumn: Option[Int] = None,
    partnumberColumn: Option[Int] = None,
    warrantyColumn: Option[Int] = None,
    brandColumn: Option[Int] = None,
    goodidSupplierColumn: Option[Int] = None,
    categoryColumns: Option[String] = None,
    descriptionColumn: Option[Int] = None,
    imageUrlColumn: Option[Int] = None,
    warrantyInMonths: Option[Boolean] = None,
    modelColumn: Option[Int] = None,
    amountColumn: Int,
    currencySignColumn: Option[Int] = None,
    currencyIdSign: Option[String] = None,
    costRrzCurrencyId: Option[Long] = None)(implicit session: DBSession = autoSession): ExcelSetting = {
    val generatedKey = withSQL {
      insert.into(ExcelSetting).columns(
        column.supplier,
        column.titleColumn,
        column.sheetNumber,
        column.title,
        column.costColumn,
        column.costCurrencyId,
        column.costRrzColumn,
        column.partnumberColumn,
        column.warrantyColumn,
        column.brandColumn,
        column.goodidSupplierColumn,
        column.categoryColumns,
        column.descriptionColumn,
        column.imageUrlColumn,
        column.warrantyInMonths,
        column.modelColumn,
        column.amountColumn,
        column.currencySignColumn,
        column.currencyIdSign,
        column.costRrzCurrencyId
      ).values(
        supplier,
        titleColumn,
        sheetNumber,
        title,
        costColumn,
        costCurrencyId,
        costRrzColumn,
        partnumberColumn,
        warrantyColumn,
        brandColumn,
        goodidSupplierColumn,
        categoryColumns,
        descriptionColumn,
        imageUrlColumn,
        warrantyInMonths,
        modelColumn,
        amountColumn,
        currencySignColumn,
        currencyIdSign,
        costRrzCurrencyId
      )
    }.updateAndReturnGeneratedKey.apply()

    ExcelSetting(
      id = generatedKey,
      supplier = supplier,
      titleColumn = titleColumn,
      sheetNumber = sheetNumber,
      title = title,
      costColumn = costColumn,
      costCurrencyId = costCurrencyId,
      costRrzColumn = costRrzColumn,
      partnumberColumn = partnumberColumn,
      warrantyColumn = warrantyColumn,
      brandColumn = brandColumn,
      goodidSupplierColumn = goodidSupplierColumn,
      categoryColumns = categoryColumns,
      descriptionColumn = descriptionColumn,
      imageUrlColumn = imageUrlColumn,
      warrantyInMonths = warrantyInMonths,
      modelColumn = modelColumn,
      amountColumn = amountColumn,
      currencySignColumn = currencySignColumn,
      currencyIdSign = currencyIdSign,
      costRrzCurrencyId = costRrzCurrencyId
    )
  }

  def create(validator: JExcelSetting): ExcelSetting = {
    import play.api.libs.json._
    import jwriters.ExcelSettingWriters.categoryColumnWrites
    import jwriters.ExcelSettingWriters.currencyIdSignWrites
    val catColumns: Option[String] = validator.categoryColumns.map{v =>
      Json.toJson(v).toString()
    }
    val currencyIdSign = validator.currencyIdSign.map(l =>
      Json.toJson(l).toString()
    )

    create(
      supplier = validator.supplier,
      titleColumn = validator.titleColumn,
      sheetNumber = validator.sheetNumber,
      title = validator.title,
      costColumn = validator.costColumn,
      costCurrencyId = validator.costCurrencyId,
      costRrzColumn = validator.costRrzColumn,
      partnumberColumn = validator.partnumberColumn,
      warrantyColumn = validator.warrantyColumn,
      brandColumn = validator.brandColumn,
      goodidSupplierColumn = validator.goodidSupplierColumn,
      categoryColumns = catColumns,
      descriptionColumn = validator.descriptionColumn,
      imageUrlColumn = validator.imageUrlColumn,
      warrantyInMonths = validator.warrantyInMonths,
      modelColumn = validator.modelColumn,
      amountColumn = validator.amountColumn,
      currencySignColumn = validator.currencySignColumn,
      currencyIdSign = currencyIdSign,
      costRrzCurrencyId = validator.costRrzCurrencyId
    )
  }

  def save(entity: ExcelSetting)(implicit session: DBSession = autoSession): ExcelSetting = {
    withSQL {
      update(ExcelSetting).set(
        column.id -> entity.id,
        column.supplier -> entity.supplier,
        column.titleColumn -> entity.titleColumn,
        column.sheetNumber -> entity.sheetNumber,
        column.title -> entity.title,
        column.costColumn -> entity.costColumn,
        column.costCurrencyId -> entity.costCurrencyId,
        column.costRrzColumn -> entity.costRrzColumn,
        column.partnumberColumn -> entity.partnumberColumn,
        column.warrantyColumn -> entity.warrantyColumn,
        column.brandColumn -> entity.brandColumn,
        column.goodidSupplierColumn -> entity.goodidSupplierColumn,
        column.categoryColumns -> entity.categoryColumns,
        column.descriptionColumn -> entity.descriptionColumn,
        column.imageUrlColumn -> entity.imageUrlColumn,
        column.warrantyInMonths -> entity.warrantyInMonths,
        column.modelColumn -> entity.modelColumn,
        column.amountColumn -> entity.amountColumn,
        column.currencySignColumn -> entity.currencySignColumn,
        column.currencyIdSign -> entity.currencyIdSign,
        column.costRrzCurrencyId -> entity.costRrzCurrencyId
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(id: Long, validator: JExcelSetting): Option[ExcelSetting] = {
    ExcelSetting.find(id).map{s =>
      import play.api.libs.json._
      import jwriters.ExcelSettingWriters.categoryColumnWrites
      import jwriters.ExcelSettingWriters.currencyIdSignWrites
      val catColumns: Option[String] = validator.categoryColumns.map{v =>
        Json.toJson(v).toString()
      }
      val currencyIdSign = validator.currencyIdSign.map(l =>
        Json.toJson(l).toString()
      )
      Some(
        s.copy(
          supplier = validator.supplier,
          titleColumn = validator.titleColumn,
          sheetNumber = validator.sheetNumber,
          title = validator.title,
          costColumn = validator.costColumn,
          costCurrencyId = validator.costCurrencyId,
          costRrzColumn = validator.costRrzColumn,
          partnumberColumn = validator.partnumberColumn,
          warrantyColumn = validator.warrantyColumn,
          brandColumn = validator.brandColumn,
          goodidSupplierColumn = validator.goodidSupplierColumn,
          categoryColumns = catColumns,
          descriptionColumn = validator.descriptionColumn,
          imageUrlColumn = validator.imageUrlColumn,
          warrantyInMonths = validator.warrantyInMonths,
          modelColumn = validator.modelColumn,
          amountColumn = validator.amountColumn,
          currencySignColumn = validator.currencySignColumn,
          currencyIdSign = currencyIdSign,
          costRrzCurrencyId = validator.costRrzCurrencyId
        ).save()
      )
    }.getOrElse(None)
  }

  def destroy(entity: ExcelSetting)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ExcelSetting).where.eq(column.id, entity.id) }.update.apply()
  }

}
