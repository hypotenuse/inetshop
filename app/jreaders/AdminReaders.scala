package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{JAdminNew, JAdminUpdate}

object AdminReaders {
  implicit val adminNewReads: Reads[JAdminNew] =
    (
      (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "email").read[String](minLength[String](5)) and
      (JsPath \ "pass").read[String](minLength[String](8))
    )(JAdminNew.apply _)


  implicit val adminUpdateReads: Reads[JAdminUpdate] = (
        (JsPath \ "name").readNullable[String](minLength[String](2)) and
      (JsPath \ "email").readNullable[String](minLength[String](5)) and
      (JsPath \ "pass").readNullable[String](minLength[String](8)) and
      (JsPath \ "oldpass").readNullable[String](minLength[String](8))
    )(JAdminUpdate.apply _)
}
