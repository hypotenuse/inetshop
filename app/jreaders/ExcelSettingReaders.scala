package jreaders

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{CategoryColumn, CurrencyIdSign, JExcelSetting}

object ExcelSettingReaders {

  implicit val currencyIdSignReads: Reads[CurrencyIdSign] = (
    (JsPath \ "currencyId").read[Long] and
    (JsPath \ "sign").read[String]
    )(CurrencyIdSign.apply _)

  implicit val categoryColumnsReads: Reads[CategoryColumn] = (
    __ \ "column").read[Int].map { CategoryColumn(_) }

  implicit val excelSettingReads: Reads[JExcelSetting] = (
      (JsPath \ "supplier").read[Long] and
      (JsPath \ "titleColumn").read[Int](max(99)) and
      (JsPath \ "sheetNumber").read[Int](max(99)) and
      (JsPath \ "title").read[String](minLength[String](2)) and
      (JsPath \ "costColumn").read[Int](max(99)) and
      (JsPath \ "costCurrencyId").readNullable[Long] and
      (JsPath \ "costRrzColumn").readNullable[Int](max(99)) and
      (JsPath \ "partnumberColumn").readNullable[Int](max(99)) and
      (JsPath \ "warrantyColumn").readNullable[Int](max(99)) and
      (JsPath \ "brandColumn").readNullable[Int](max(99)) and
      (JsPath \ "goodidSupplierColumn").readNullable[Int](max(99)) and
      (JsPath \ "categoryColumns").readNullable[List[CategoryColumn]] and
      (JsPath \ "descriptionColumn").readNullable[Int](max(99)) and
      (JsPath \ "imageUrlColumn").readNullable[Int](max(99)) and
      (JsPath \ "warrantyInMonths").readNullable[Boolean] and
      (JsPath \ "modelColumn").readNullable[Int](max(99)) and
      (JsPath \ "amountColumn").read[Int](max(99)) and
      (JsPath \ "currencySignColumn").readNullable[Int](max(99)) and
      (JsPath \ "currencyIdSigns").readNullable[List[CurrencyIdSign]] and
      (JsPath \ "costRrzCurrencyId").readNullable[Long]
    )(JExcelSetting.apply _).filterNot(
    ValidationError("currencySignColumn_and_currencyIdSign_should_be_filled_together")
  ) { obj =>
    (obj.currencySignColumn.nonEmpty || obj.currencyIdSign.nonEmpty) && !(obj.currencyIdSign.nonEmpty && obj.currencySignColumn.nonEmpty)
  }.filterNot(
    ValidationError("costRrzCurrencyId_and_costRrzColumn_should_be_filled_together")
  ) { obj =>
    (obj.costRrzCurrencyId.nonEmpty || obj.costRrzColumn.nonEmpty) && !(obj.costRrzCurrencyId.nonEmpty && obj.costRrzColumn.nonEmpty)
  }.filterNot(
    ValidationError("only_one_of_costCurrencyId_or_currencySignColumn_should_be_filled")
  ) { obj =>
    obj.costCurrencyId.nonEmpty && obj.currencySignColumn.nonEmpty
  }
}
