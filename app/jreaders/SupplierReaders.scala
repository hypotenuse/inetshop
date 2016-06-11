package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators._

object SupplierReaders {

  implicit val currencyReads: Reads[JSupplierCurrency] = (
      (JsPath \ "id").read[Long] and
      (JsPath \ "rate").readNullable[BigDecimal]
    )(JSupplierCurrency.apply _)

  implicit val supplierNewReads: Reads[JSupplierNew] =
    (__ \ "title").read[String](minLength[String](2)).map { JSupplierNew(_) }


  implicit val supplierUpdateReads: Reads[JSupplierUpdate] = (
      (JsPath \ "title").readNullable[String](minLength[String](2)) and
      (JsPath \ "info").readNullable[String](maxLength[String](20000)) and
      (JsPath \ "currencies").readNullable[List[JSupplierCurrency]]
    )(JSupplierUpdate.apply _)

}
