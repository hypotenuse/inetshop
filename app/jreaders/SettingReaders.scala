package jreaders

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{JSettingNew, JSettingUpdate}

object SettingReaders {

  val onlyLatin: Reads[String] =
    Reads.StringReads.filter(ValidationError("ONLY_LATIN_CHARACTERS"))(str => {
      str.matches("""[a-zA-Z]+""")
    })

  implicit val settingNewReads: Reads[JSettingNew] =
    (
      (JsPath \ "title").read[String](minLength[String](2)) and
      (JsPath \ "value").read[String](minLength[String](2)) and
      (JsPath \ "shortcode").read[String](onlyLatin)
    )(JSettingNew.apply _)


  implicit val settingUpdateReads: Reads[JSettingUpdate] = (
        (JsPath \ "title").readNullable[String](minLength[String](2)) and
      (JsPath \ "value").readNullable[String](minLength[String](2)) and
      (JsPath \ "shortcode").readNullable[String](minLength[String](2))
    )(JSettingUpdate.apply _)
}
