package controllers.admin


import jwriters.GoodWriters._
import models.{Supplier, SupplierGoodsMap, Good, RawSupplierGood}

import play.api.libs.json._
import play.api.mvc.Action
import repositories.{RawGoodListAggregate}

class RawGoods extends BaseSecuredController {


  def list(length: Int, start: Int, draw: Int) = admin(
    admin =>
      Action {
        implicit request =>
          val supplier = request.queryString.get("supplierId").map(v => Some(v.head.toLong)).getOrElse(None)
//          println("search" + request.queryString.get("search[value]"))
          val search = request.queryString.get("search[value]").map(v => Some(v.head)).getOrElse(None)
          val (goods, countFiltered, countTotal) = RawGoodListAggregate.list(length, start, supplier, search)
          val result = Json.obj(
            "data" -> goods,
            "recordsFiltered" -> JsNumber(countFiltered),
            "recordsTotal" -> JsNumber(countTotal),
            "draw" -> draw
          )
          Ok(Json.toJson(result))
      }
  )

  def view(id: Long) = admin(
    admin =>
      Action {
        RawSupplierGood.find(id).map {
          import jwriters.RawSupplierGoodWriters.rawGoodWrites
          rawSupplierGood => Ok(Json.toJson(rawSupplierGood))
        }.getOrElse(NotFound)
      }
  )

  def linkToGood(rawId: Long, goodId: Long) = admin(
    admin =>
      Action {
        val data: Option[(RawSupplierGood, Good)] = for{
          raw <- RawSupplierGood.find(rawId)
          good <- Good.find(goodId)
          supplier <- Supplier.find(raw.supplier)
        } yield (raw, good)
        data.map{case (raw, good) =>
          import scalikejdbc._
          SupplierGoodsMap.findBy(sqls"text = ${raw.text} and supplier = ${raw.supplier}").getOrElse{
            SupplierGoodsMap.create(
            text = raw.text,
              good = good.id,
              supplier = raw.supplier
            )
          }
          Ok(Json.toJson(JsString("Success")))
        }.getOrElse(NotFound)

      }
  )


}
