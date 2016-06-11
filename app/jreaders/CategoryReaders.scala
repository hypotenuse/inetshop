package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{JCategoryUpdate, JCategoryNew}

object CategoryReaders {
  implicit val categoryNewReads: Reads[JCategoryNew] =
    (
      (JsPath \ "title").read[String](minLength[String](2)) and
      (JsPath \ "parentId").readNullable[Long]
    )(JCategoryNew.apply _)


  implicit val categoryUpdateReads: Reads[JCategoryUpdate] = (
      (JsPath \ "languagecod").read[String](maxLength[String](250)) and
      (JsPath \ "title").readNullable[String](minLength[String](2)) and
      (JsPath \ "description").readNullable[String](maxLength[String](4000000)) and
      (JsPath \ "metatitle").readNullable[String](maxLength[String](250)) and
      (JsPath \ "metadescription").readNullable[String](maxLength[String](250)) and
      (JsPath \ "slug").readNullable[String](minLength[String](2) keepAnd maxLength[String](250)) and
      (JsPath \ "onhome").readNullable[Boolean] and
      (JsPath \ "parentId").readNullable[Long]
    )(JCategoryUpdate.apply _)
}
