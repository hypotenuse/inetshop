package jwriters

import models.RawSupplierGood
import play.api.libs.json.{Writes, _}


object RawSupplierGoodWriters {


  implicit val rawGoodWrites = new Writes[RawSupplierGood] {
    def writes(good: RawSupplierGood) = Json.obj(
      "title" -> good.title,
      "partnumber" -> good.partnumber,
      "brand" -> good.brand,
      "category" -> good.category,
      "description" -> good.description,
      "model" -> good.model
    )
  }
}

