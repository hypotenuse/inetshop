package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{JNewsUpdate, JNewsNew}

object NewsReaders {
  implicit val newsNewReads: Reads[JNewsNew] =
    (__ \ "title").read[String](minLength[String](2)).map { JNewsNew(_) }


  implicit val newsUpdateReads: Reads[JNewsUpdate] = (
      (JsPath \ "languagecod").read[String](maxLength[String](5)) and
      (JsPath \ "title").readNullable[String](minLength[String](2)) and
      (JsPath \ "content").readNullable[String](maxLength[String](4000000))
    )(JNewsUpdate.apply _)
}
