package messages

import play.api.libs.json.{Writes, Json}


case class Statuses(statuses: List[Status], histories: List[History])

case class Histories(statuses: List[History])

case class History(time: String, supplier: String, processed: Int)

case class Status(status: String, supplier: String, processed: Int)

object Status {
  implicit val priceStatusFormat = Json.format[Status]
}

object History {
  implicit val priceHistoryFormat = Json.format[History]
}




