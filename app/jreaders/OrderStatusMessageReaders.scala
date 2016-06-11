package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{JOrderStatusMessageNew, JOrderStatusMessageUpdate}

object OrderStatusMessageReaders {
  implicit val orderStatusMessageNewReads: Reads[JOrderStatusMessageNew] =
    (
      (JsPath \ "orderstatusid").read[Long] and
      (JsPath \ "messagetitle").read[String](minLength[String](2)) and
      (JsPath \ "messagetext").read[String](minLength[String](2)) and
      (JsPath \ "forclient").read[Boolean]
    )(JOrderStatusMessageNew.apply _)


  implicit val orderStatusMessageUpdateReads: Reads[JOrderStatusMessageUpdate] = (
      (JsPath \ "orderstatusid").read[Long] and
      (JsPath \ "languagecod").read[String](maxLength[String](250)) and
      (JsPath \ "messagetitle").readNullable[String](minLength[String](2)) and
      (JsPath \ "messagetext").readNullable[String](minLength[String](2)) and
      (JsPath \ "forclient").read[Boolean]
    )(JOrderStatusMessageUpdate.apply _)




}
