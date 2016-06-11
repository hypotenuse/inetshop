package messages

import play.api.libs.json.Json


case class NewPrice (supplierId: Long, filename: String, settingId: Long)

object NewPrice{
  implicit val priceFormat = Json.format[NewPrice]
}