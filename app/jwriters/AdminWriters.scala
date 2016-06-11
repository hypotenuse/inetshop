package jwriters

import models.Admin
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{Writes, _}


object AdminWriters {
  implicit val adminWriters = new Writes[Admin] {
    val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
    def writes(admin: Admin) = Json.obj(
      "id" -> admin.id,
      "name" -> admin.name,
      "email" -> admin.email,
      "lastvisit" -> admin.lastvisit.map(format.print(_))
    )
  }

}
