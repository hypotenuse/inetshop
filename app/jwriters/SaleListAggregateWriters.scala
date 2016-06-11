package jwriters

import play.api.libs.json.{Writes, _}
import repositories.SaleListAggregate


object SaleListAggregateWriters {
  implicit val saleWrites = new Writes[SaleListAggregate] {
    def writes(sale: SaleListAggregate) =
      JsArray(
        Seq(
          JsNumber(sale.id),
          JsString(sale.title)
        )
      )
  }

}

