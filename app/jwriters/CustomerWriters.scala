package jwriters

import models.Customer
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{Writes, _}


object CustomerWriters {
  implicit val customerWrites = new Writes[Customer] {
    val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
    def writes(customer: Customer) = Json.obj(
      "id" -> customer.id,
      "name" -> customer.name,
      "email" -> customer.email,
      "phone" -> customer.phone,
      "spent" -> customer.spent,
      "lastvisit" -> customer.lastvisit.map(format.print(_))
    )
  }

}
