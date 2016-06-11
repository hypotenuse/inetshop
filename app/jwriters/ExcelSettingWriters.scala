package jwriters

import models.ExcelSetting
import play.api.libs.json.{Writes, _}
import validators.{CurrencyIdSign, CategoryColumn}


object ExcelSettingWriters {

  implicit val categoryColumnWrites = new Writes[CategoryColumn] {
    def writes(catColumn: CategoryColumn) = Json.obj(
    "column" -> catColumn.column
    )
  }

  implicit val currencyIdSignWrites = new Writes[CurrencyIdSign] {
    def writes(curSign: CurrencyIdSign) = Json.obj(
    "currency" -> curSign.currency,
    "sign" -> curSign.sign
    )
  }

  implicit val excelSettingWrites = new Writes[ExcelSetting] {
    def writes(excelSetting: ExcelSetting) = Json.obj(
    "id" -> excelSetting.id,
      "supplier" -> excelSetting.supplier,
      "titleColumn" -> excelSetting.titleColumn,
      "sheetNumber" -> excelSetting.sheetNumber,
      "title" -> excelSetting.title,
      "costColumn" -> excelSetting.costColumn,
      "costCurrencyId" -> excelSetting.costCurrencyId,
      "costRrzCurrencyId" -> excelSetting.costRrzCurrencyId,
      "costRrzColumn" -> excelSetting.costRrzColumn,
      "partnumberColumn" -> excelSetting.partnumberColumn,
      "warrantyColumn" -> excelSetting.warrantyColumn,
      "brandColumn" -> excelSetting.brandColumn,
      "goodidSupplierColumn" -> excelSetting.goodidSupplierColumn,
      "categoryColumns" -> excelSetting.categoryColumns.map(v => Json.parse(v)),
      "descriptionColumn" -> excelSetting.descriptionColumn,
      "imageUrlColumn" -> excelSetting.imageUrlColumn,
      "warrantyInMonths" -> excelSetting.warrantyInMonths,
      "modelColumn" -> excelSetting.modelColumn,
      "amountColumn" -> excelSetting.amountColumn,
      "currencySignColumn" -> excelSetting.currencySignColumn,
      "currencyIdSign" -> excelSetting.currencyIdSign.map(v => Json.parse(v))
    )
  }




  implicit val ExcelSettingListWrites = new Writes[ExcelSetting] {
    def writes(excelSettingList: ExcelSetting) =
      JsArray(
        Seq(
          JsNumber(excelSettingList.id),
          JsString(excelSettingList.title)
        ))
  }
}
