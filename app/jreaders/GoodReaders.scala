package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{JGoodAddFromRaw, JGoodPriceUpdate, JGoodNew, JGoodUpdate}

object GoodReaders {
  implicit val goodNewReads: Reads[JGoodNew] =
    (__ \ "title").read[String](minLength[String](2)).map { JGoodNew(_) }


  implicit val goodUpdateReads: Reads[JGoodUpdate] = (
      (JsPath \ "languagecod").read[String](maxLength[String](250)) and
      (JsPath \ "title").readNullable[String](minLength[String](2)) and
      (JsPath \ "description").readNullable[String](maxLength[String](4000000)) and
      (JsPath \ "descriptionShort").readNullable[String](maxLength[String](250)) and
      (JsPath \ "metatitle").readNullable[String](maxLength[String](250)) and
      (JsPath \ "partnumber").readNullable[String](maxLength[String](250)) and
      (JsPath \ "metadescription").readNullable[String](maxLength[String](250)) and
      (JsPath \ "cost").readNullable[BigDecimal](max[BigDecimal](99999999.99)) and
      (JsPath \ "manufacturer").readNullable[Long] and
      (JsPath \ "warranty").readNullable[Int](max[Int](99)) and
      (JsPath \ "import_short_desc").readNullable[Boolean] and
      (JsPath \ "newg").readNullable[Boolean] and
      (JsPath \ "top").readNullable[Boolean] and
      (JsPath \ "slug").readNullable[String](minLength[String](2) keepAnd maxLength[String](250))
    )(JGoodUpdate.apply _)

  implicit val goodUpdatePriceReads: Reads[JGoodPriceUpdate] = (
      (JsPath \ "category").readNullable[Long] and
      (JsPath \ "manufacturer").readNullable[Long] and
      (JsPath \ "costfrom").readNullable[Int] and
      (JsPath \ "costto").readNullable[Int] and
      (JsPath \ "formula").read[String](maxLength[String](4)) and
      (JsPath \ "search").readNullable[String]
    )(JGoodPriceUpdate.apply _)


  implicit val goodAddFromRawReads: Reads[JGoodAddFromRaw] = (
      (JsPath \ "title").read[String](minLength[String](2)) and
      (JsPath \ "description").readNullable[String](maxLength[String](4000000)) and
      (JsPath \ "descriptionShort").readNullable[String](maxLength[String](250)) and
      (JsPath \ "manufacturer").readNullable[Long] and
      (JsPath \ "partnumber").readNullable[String](maxLength[String](250)) and
      (JsPath \ "categories").read[List[Long]]
    )(JGoodAddFromRaw.apply _)

}
