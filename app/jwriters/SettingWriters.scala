package jwriters

import models.Setting
import play.api.libs.json.{Writes, _}


object SettingWriters {
  implicit val settingWrites = new Writes[Setting] {
    def writes(setting: Setting) = Json.obj(
      "id" -> setting.id,
      "title" -> setting.title,
      "value" -> setting.value,
      "shortcode" -> setting.shortcode
    )
  }
}
