package repositories

import play.api.libs.json.Json


case class PriceStatusListAggregate (filename: String, status: String, excelSettingTitle: String, excelSettingId: Long)

object PriceStatusListAggregate{
  implicit val priceStatusFormat = Json.format[PriceStatusListAggregate]
}