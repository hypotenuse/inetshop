package jwriters

import models.Currency
import play.api.libs.json.{Writes, _}


object CurrencyWriters {
  implicit val currencyWrites = new Writes[Currency] {
    def writes(currency: Currency) = Json.obj(
      "id" -> currency.id,
      "title" -> currency.title,
      "main" -> currency.main,
      "cros" -> currency.cros
    )
  }

}
