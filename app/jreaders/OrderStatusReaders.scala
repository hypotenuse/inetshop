package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators._

object OrderStatusReaders {
  implicit val orderStatusNewReads: Reads[JOrderStatusNew] =
    (__ \ "title").read[String](minLength[String](2)).map { JOrderStatusNew(_) }


  implicit val orderStatusUpdateReads: Reads[JOrderStatusUpdate] = (
      (JsPath \ "languagecod").read[String](maxLength[String](250)) and
      (JsPath \ "title").readNullable[String](minLength[String](2)) and
      (JsPath \ "sendmessageClient").read[Boolean] and
      (JsPath \ "sendmessageAdmin").read[Boolean]
    )(JOrderStatusUpdate.apply _)

}
