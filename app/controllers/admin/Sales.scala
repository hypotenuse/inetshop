package controllers.admin

import java.util.NoSuchElementException

import jreaders.SaleReaders._
import jwriters.SaleWriters._
import models.{Good, Sale}
import play.Logger
import play.api.libs.json._
import play.api.mvc.Action
import repositories.{SaleEditAggregate, SaleListAggregate}
import validators.{JSaleNew, JSaleUpdate}

class Sales extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JSaleNew].fold(
          valid = { jSaleNew =>
            val sale=Sale.create(jSaleNew)
            Ok(Json.toJson(JsNumber(sale.id)))
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
        json.validate[JSaleUpdate].fold(
          valid = { jSale =>
            try {
              Sale.save(id, jSale)
              Ok(Json.toJson(JsString("SAVED")))
            }
            catch {
              case e: NoSuchElementException => NotFound
              case e: IllegalArgumentException => BadRequest(Json.toJson("TITLE_IS_REQUIRED"))
            }
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )

      }
  )


  def addGoods(id: Long) = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        (json \ "goods").validate[List[Long]].fold(
          valid = { list =>
            Sale.find(id).map{
              sale =>
                val goods: List[Good] = list.flatMap(Good.find(_))
                goods.map(sale.addGood(_))
                Ok(Json.toJson(JsString("SAVED")))
            }.getOrElse(NotFound)
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )

      }
  )

  def removeGoods(id: Long) = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        (json \ "goods").validate[List[Long]].fold(
          valid = { list =>
            Sale.find(id).map{
              sale =>
                val goods: List[Good] = list.flatMap(Good.find(_))
                goods.map(sale.removeGood(_))
                Ok(Json.toJson(JsString("SAVED")))
            }.getOrElse(NotFound)
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )

      }
  )

  def list = admin(
    admin =>
      Action { implicit request =>
        import jwriters.SaleListAggregateWriters._
        val result = Json.obj("data" -> SaleListAggregate.list())
        Ok(Json.toJson(result))
      }
  )


  def view(id: Long) = admin(
    admin =>
      Action {
        SaleEditAggregate.get(id).map{
          n => Ok(Json.toJson(n))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        Sale.find(id).map{
          sale =>
              sale.destroy()
              Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )


  def notInSaleGoodsList(id: Long, length: Int, start: Int, draw: Int) = admin(
    admin =>
      Action {
        implicit request =>
            Sale.find(id).map{s=>

                import repositories.GoodSearch
                import models.Language
                import scalikejdbc._
                import jwriters.GoodWriters._

                val columns: Vector[String] =
                  Vector("id", "title", "partnumber", "manufacturer", "manufacturerid", "cost", "warranty", "supplierid", "supplier", "import_short_desc")
                val search = request.queryString.get("search[value]").map(v => Some(v.head)).getOrElse(None)
                val manufacturer = request.queryString.get("manufacturer").map(v => Some(v.head.toLong)).getOrElse(None)
                val category = request.queryString.get("category").map(v => Some(v.head.toLong)).getOrElse(None)
                val costTo = request.queryString.get("costto").map(v => Some(v.head.toInt)).getOrElse(None)
                val costFrom = request.queryString.get("costfrom").map(v => Some(v.head.toInt)).getOrElse(None)
                val order = request.queryString.get("order[0][dir]").map(v => v.head).getOrElse("asc")
                val orderColumn = request.queryString.get("order[0][column]").map(v => v.head.toInt).getOrElse(0)
                Language.findBy(sqls"defaultlng = true").map{
                  l =>
                    val (goodList, count, countTotal) = GoodSearch.getListNotInSale(s, l.id, length, start, order, columns(orderColumn), search, manufacturer, category, costTo, costFrom)
                    val result = Json.obj(
                      "data" -> goodList,
                      "recordsFiltered" -> JsNumber(count),
                      "recordsTotal" -> JsNumber(countTotal),
                      "draw" -> draw
                    )
                    Ok(Json.toJson(result))

                }.getOrElse(Ok(Json.toJson(Json.obj("error" -> "Default language not found!"))))
            }.getOrElse(NotFound)


      }
  )


  def inSaleGoodsList(id: Long, length: Int, start: Int, draw: Int) = admin(
    admin =>
      Action {
        implicit request =>
            Sale.find(id).map{s=>

                import repositories.GoodSearch
                import models.Language
                import jwriters.GoodWriters._

                Language.getDefault.map{
                  l =>
                    val columns: Vector[String] =
                      Vector("id", "title", "partnumber", "manufacturer", "manufacturerid", "cost", "warranty", "supplierid", "supplier", "import_short_desc")
                    val search = request.queryString.get("search[value]").map(v => Some(v.head)).getOrElse(None)
                    val manufacturer = request.queryString.get("manufacturer").map(v => Some(v.head.toLong)).getOrElse(None)
                    val category = request.queryString.get("category").map(v => Some(v.head.toLong)).getOrElse(None)
                    val costTo = request.queryString.get("costto").map(v => Some(v.head.toInt)).getOrElse(None)
                    val costFrom = request.queryString.get("costfrom").map(v => Some(v.head.toInt)).getOrElse(None)
                    val order = request.queryString.get("order[0][dir]").map(v => v.head).getOrElse("asc")
                    val orderColumn = request.queryString.get("order[0][column]").map(v => v.head.toInt).getOrElse(0)

                    val (goodList, count, countTotal) = GoodSearch.getListInSale(s.id, l.id, length, start, order, columns(orderColumn), search, manufacturer, category, costTo, costFrom)
                    val result = Json.obj(
                      "data" -> goodList,
                      "recordsFiltered" -> JsNumber(count),
                      "recordsTotal" -> JsNumber(countTotal),
                      "draw" -> draw
                    )
                    Ok(Json.toJson(result))

                }.getOrElse(Ok(Json.toJson(Json.obj("error" -> "Default language not found!"))))
            }.getOrElse(NotFound)


      }
  )
}
