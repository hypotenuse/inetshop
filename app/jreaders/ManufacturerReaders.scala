package jreaders

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{JManufacturerNew, JManufacturerUpdate}

object ManufacturerReaders {
  implicit val manufacturerReads: Reads[JManufacturerNew] =
  (__ \ "title").read[String](minLength[String](2)).map { JManufacturerNew(_) }


  implicit val ManufacturerUpdateReads: Reads[JManufacturerUpdate] = (
      (JsPath \ "languagecod").read[String](maxLength[String](250)) and
      (JsPath \ "title").readNullable[String](minLength[String](2)) and
      (JsPath \ "description").readNullable[String](maxLength[String](2000))
    )(JManufacturerUpdate.apply _)
}
