package messages

import play.api.libs.json.Json

case class PriceStatus(price: NewPrice, status: String, processed: Int)

object PriceStatus {
  implicit val priceStatusFormat = Json.format[PriceStatus]
}
