package controllers.admin

import java.util.NoSuchElementException

import models._
import play.api.Logger
import play.api.mvc.Action
import play.api.libs.json._
import repositories.GoodEditAggregate
import validators.{JGoodAddFromRaw, JGoodPriceUpdate, JGoodUpdate, JGoodNew}
import scalikejdbc._
import jwriters.GoodWriters._
import jreaders.GoodReaders._

class Goods extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JGoodNew].fold(
          valid = { jGood =>
            val good=Good.create(jGood)
            Ok(Json.toJson(JsNumber(good.id)))
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )
      }
  )


  def addFromRaw() = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JGoodAddFromRaw].fold(
          valid = { jGood =>
            val good=Good.create(jGood)
            Ok(Json.toJson(JsNumber(good.id)))
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )
      }
  )

  def update(id: Long) = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JGoodUpdate].fold(
          valid = { jGood =>
            try {
              Good.save(id, jGood)
              Ok(Json.toJson(JsString("Saved")))
            }
            catch {
              case e: NoSuchElementException => NotFound
              case e: IllegalArgumentException => BadRequest(Json.toJson(JsObject(Map("obj.title" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.required")))))))))
            }
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )

      }
  )

  def list(length: Int, start: Int, draw: Int) = admin(
    admin =>
      Action {
        implicit request =>
          import repositories.GoodSearch

          val columns: Vector[String] =
            Vector("id", "title", "partnumber", "manufacturer", "manufacturerid", "cost", "supplierСost", "warranty", "supplierid", "supplier", "import_short_desc")
          val search = request.queryString.get("search[value]").map(v => Some(v.head)).getOrElse(None)
          val manufacturer = request.queryString.get("manufacturer").map(v => Some(v.head.toLong)).getOrElse(None)
          val category = request.queryString.get("category").map(v => Some(v.head.toLong)).getOrElse(None)
          val costTo = request.queryString.get("costto").map(v => Some(v.head.toInt)).getOrElse(None)
          val costFrom = request.queryString.get("costfrom").map(v => Some(v.head.toInt)).getOrElse(None)
          val order = request.queryString.get("order[0][dir]").map(v => v.head).getOrElse("asc")
          val orderColumn = request.queryString.get("order[0][column]").map(v => v.head.toInt).getOrElse(0)
          Language.findBy(sqls"defaultlng = true").map{
            l =>
              val (goodList, count, countTotal) = GoodSearch.getList(l.id, length, start, order, columns(orderColumn), search, manufacturer, category, costTo, costFrom)
              val result = Json.obj(
                "data" -> goodList,
                "recordsFiltered" -> JsNumber(count),
                "recordsTotal" -> JsNumber(countTotal),
                "draw" -> draw
              )
              Ok(Json.toJson(result))

          }.getOrElse(Ok(Json.toJson(Json.obj("error" -> "Default language not found!"))))

      }
  )

  def view(id: Long) = admin(
    admin =>
      Action {
        GoodEditAggregate.get(id).map{
          a => Ok(Json.toJson(a))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        Good.find(id).map{
          g =>
            g.destroy()
            Ok(Json.toJson(Json.obj("status" -> "success")))
        }.getOrElse(NotFound)
      }
  )

  def setCategories(id: Long) = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        (json \ "categories").validate[List[Long]].fold(
          valid = { list =>
            Good.find(id).map{
              good =>
                val categories: List[Category] = list.flatMap(Category.find(_))
                good.removeCategories()
                categories.map(good.addCategory(_))
                Ok(Json.toJson(JsString("SAVED")))
            }.getOrElse(NotFound)
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )

      }
  )

  def updatePrices() = admin(
  admin =>
    Action(parse.json) { implicit request =>
      val json = request.body
      json.validate[JGoodPriceUpdate].fold(
        valid = { jPriceUpdate =>
          import repositories.GoodSearch
          Language.findBy(sqls"defaultlng = true").map {
            l =>
              if(!jPriceUpdate.formula.contains("+") && !jPriceUpdate.formula.contains("-")) BadRequest(Json.toJson(JsString("INCORRECT_FORMULA")))
              val percent = jPriceUpdate.formula.replace("+", "").replace("-", "").toInt
              val (listAggregates, _, _) = GoodSearch.getList(
                languageid = l.id,
                length = 999999,
                offset = 0,
                order = "asc",
                orderColumn = "id",
                searchText = jPriceUpdate.search,
                manufacturerId = jPriceUpdate.manufacturer,
            categoryId = jPriceUpdate.category,
            costTo = jPriceUpdate.costto,
            costFrom = jPriceUpdate.costfrom
              )

              listAggregates.map {
                goodListAggregate =>
                  val id = goodListAggregate.id
                  val nole: BigDecimal = BigDecimal(0.0)
                  val cost: BigDecimal = goodListAggregate.cost.map(scala.math.BigDecimal(_)).getOrElse(scala.math.BigDecimal(0))

                  if(cost > 0){
                    Good.find(id).map{ g =>
                      if(jPriceUpdate.formula.contains("+")){
                        g.copy(cost = g.cost + g.cost * percent / 100).save
                      }
                      else {
                        g.copy(cost = g.cost - g.cost * percent / 100).save
                      }
                    }
                  }

              }
          }.getOrElse(Ok(Json.toJson(Json.obj("error" -> "Default language not found!"))))
          Ok(Json.toJson(JsString("SUCCESS")))
        },
        invalid = {
          errors => BadRequest(JsError.toJson(errors))
        }
      )
    }
  )
}
