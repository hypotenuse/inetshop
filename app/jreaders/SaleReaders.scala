package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{JSaleNew, JSaleUpdate}

object SaleReaders {
  implicit val saleNewReads: Reads[JSaleNew] =
    (
      (JsPath \ "title").read[String](minLength[String](2)) and
      (JsPath \ "titlecolorbackgrnd").read[String](minLength[String](3))
    )(JSaleNew.apply _)


  implicit val saleUpdateReads: Reads[JSaleUpdate] = (
      (JsPath \ "languagecod").read[String](maxLength[String](5)) and
      (JsPath \ "title").readNullable[String](minLength[String](2)) and
      (JsPath \ "text").readNullable[String](maxLength[String](4000000)) and
      (JsPath \ "titlecolorbackgrnd").readNullable[String](minLength[String](3))
    )(JSaleUpdate.apply _)
}
