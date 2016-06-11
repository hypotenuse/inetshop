package jwriters

import models.Supplier
import play.api.libs.json.{Writes, _}
import repositories.{SupplierCurrency, SupplierEdit}


object SupplierWriters {


  implicit val currenciesWrites = new Writes[SupplierCurrency] {
    def writes(currency: SupplierCurrency) =
      Json.obj(
        "id" -> JsNumber(currency.id),
        "title" -> JsString(currency.title),
        "rate" -> currency.rate.map(JsNumber(_))
      )
  }

  implicit val supplierWrites = new Writes[Supplier] {
    def writes(supplier: Supplier) = Json.obj(
      "id" -> supplier.id,
      "title" -> supplier.title,
      "info" -> supplier.info
    )
  }

  implicit val supplierEditWrites = new Writes[SupplierEdit] {
    def writes(supplierEdit: SupplierEdit) = Json.obj(
      "supplier" -> Json.toJson(supplierEdit.supplier),
      "currencies" -> Json.toJson(supplierEdit.currencies)
    )
  }


}
