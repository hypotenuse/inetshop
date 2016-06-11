package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators._

object CurrencyReaders {
  implicit val currencyNewReads: Reads[JCurrencyNew] =
    (
      (JsPath \ "title").read[String](minLength[String](2)) and
      (JsPath \ "main").read[Boolean] and
      (JsPath \ "cros").read[BigDecimal](max[BigDecimal](99.99))
    )(JCurrencyNew.apply _)


  implicit val currencyUpdateReads: Reads[JCurrencyUpdate] = (
         (JsPath \ "title").readNullable[String](minLength[String](2)) and
      (JsPath \ "main").readNullable[Boolean] and
      (JsPath \ "cros").readNullable[BigDecimal](max[BigDecimal](99.99))
    )(JCurrencyUpdate.apply _)

}
