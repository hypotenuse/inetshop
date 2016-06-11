package jwriters

import models.{Sale, SaleText}
import play.api.libs.json.{Writes, _}
import repositories.SaleEdit


object SaleWriters {
  implicit val saleWrites = new Writes[Sale] {
    def writes(sale: Sale) = Json.obj(
      "id" -> sale.id,
      "titlecolorbackgrnd" -> sale.titlecolorbackgrnd
    )
  }

  implicit val saleTextWrites = new Writes[SaleText] {
    def writes(saleText: SaleText) = Json.obj(
      "saleid" -> saleText.saleid,
      "languageid" -> saleText.languageid,
      "title" -> saleText.title,
      "text" -> saleText.text
    )
  }

  implicit val SaleEditWrites = new Writes[SaleEdit] {
    def writes(saleEdit: SaleEdit) = Json.obj(
      "sale" -> Json.toJson(saleEdit.sale),
      "data" -> Json.toJson(
        saleEdit.data.toMap
      )
    )
  }
}
