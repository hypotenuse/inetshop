package jwriters

import messages.Statuses
import play.api.libs.json.{Writes, _}

object StatusWriters {
  implicit val priceStatusesWriter = new Writes[Statuses] {
    def writes(statuses: Statuses) = Json.obj(
      "status" -> statuses.statuses,
      "history" -> statuses.histories
    )
  }
}
