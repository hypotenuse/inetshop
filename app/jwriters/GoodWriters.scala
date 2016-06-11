package jwriters

import models.{Language, ManufacturerText, GoodText, Good}
import play.api.libs.json.Writes
import play.api.libs.json._
import repositories.{GoodListAggregate, GoodEdit}


object GoodWriters {

  import jwriters.CategoryWriters.categorySelectAggregateWrites

  implicit val goodWrites = new Writes[Good] {
    def writes(good: Good) = Json.obj(
      "id" -> good.id,
      "partnumber" -> good.partnumber,
      "cost" -> good.cost,
      "supplierid" -> good.supplierid,
      "suppliercost" -> good.suppliercost,
      "manufacturer" -> good.manufacturer,
      "import_short_desc" -> good.import_short_desc,
      "top" -> good.top,
      "newg" -> good.newg,
      "warranty" -> good.warranty,
      "slug" -> good.slug
    )
  }

  implicit val goodTextWrites = new Writes[GoodText] {
    def writes(goodText: GoodText) = Json.obj(
      "goodid" -> goodText.goodid,
      "languageid" -> goodText.languageid,
      "title" -> goodText.title,
      "description" -> goodText.description,
      "descriptionShort" -> goodText.descriptionShort,
      "metatitle" -> goodText.metatitle,
      "metadescription" -> goodText.metadescription
    )
  }

  implicit val GoodEditWrites = new Writes[GoodEdit] {
    def writes(goodEdit: GoodEdit) = Json.obj(
      "good" -> Json.toJson(goodEdit.good),
      "data" -> Json.toJson(
        goodEdit.data.toMap
      ),
      "categories" -> Json.toJson(goodEdit.categories)
    )
  }

  implicit val GoodListWrites = new Writes[GoodListAggregate] {
    def writes(goodList: GoodListAggregate) =
      JsArray(
        Seq(
          JsNumber(goodList.id),
          JsString(goodList.title),
          goodList.partnumber.map(JsString(_)).getOrElse(JsNull),
          goodList.manufacturerid.map(JsNumber(_)).getOrElse(JsNull),
          goodList.manufacturerTitle.map(JsString(_)).getOrElse(JsNull),
          goodList.cost.map(JsNumber(_)).getOrElse(JsNull),
          goodList.supplierСost.map(JsNumber(_)).getOrElse(JsNull),
          goodList.warranty.map(JsNumber(_)).getOrElse(JsNull),
          goodList.supplierid.map(JsNumber(_)).getOrElse(JsNull),
          goodList.supplier.map(JsString(_)).getOrElse(JsNull),
          JsBoolean(goodList.import_short_desc)
        ))
  }
}
