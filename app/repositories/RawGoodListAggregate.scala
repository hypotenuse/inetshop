package repositories

import models.{ManufacturerText, Sale}
import play.api.libs.json.Json
import scalikejdbc._
import services.Repository

import scala.util.{Failure, Success}

case class RawGoodListAggregate(id: Long, title: String, brand: Option[String])

object RawGoodListAggregate {
  implicit val session = AutoSession
  implicit val rawGoodAggregateFormat = Json.format[RawGoodListAggregate]
  val what =
    """id,
       title,
      brand
    """


  private def count(where: String = ""): Int = {
    val result: Option[Int] = if (where == "") {
      DB readOnly { implicit session =>
        sql"select COUNT(*) from raw_suppliers_goods"
          .map(rs => rs.int("count")).single.apply()
      }
    }
    else {
      val whereSQL = SQLSyntax.createUnsafely(where)
      DB readOnly { implicit session =>
        sql"select COUNT(*) from raw_suppliers_goods as raw ${whereSQL}"
          .map(rs => rs.int("count")).single.apply()
      }
    }

    result.getOrElse(0)
  }

  def list(length: Int, offset: Int, supplierId: Option[Long] = None, searchText: Option[String] = None) = {

    val order = SQLSyntax.createUnsafely("updated, title asc")
    val whatSelect = SQLSyntax.createUnsafely(what)
    val where = SQLSyntax.createUnsafely("Where NOT EXISTS (SELECT map.text from suppliers_goods_map as map  where raw.text=map.text) " + supplierWhere(supplierId) + formSearchWhere(searchText))
    val whereAll = SQLSyntax.createUnsafely("Where NOT EXISTS (SELECT map.text from suppliers_goods_map as map  where raw.text=map.text)")
    val dbResult = DB readOnly { implicit session =>
      sql"select ${whatSelect} from raw_suppliers_goods as raw ${where} ORDER BY ${order} LIMIT ${length} OFFSET ${offset}"
    }

    val goods = dbResult.map {
      rs =>
        RawGoodListAggregate(
          rs.long("id"),
          rs.string("title"),
          rs.stringOpt("brand")
        )

    }.list().apply()
    val countFiltered = count(where)
    val countTotal = count(whereAll)
    (goods, countFiltered, countTotal)
  }


  private def formSearchWhere(searchText: Option[String]): String = {
    searchText.map { text =>
      if (text != ""){
        s"AND (CAST(raw.title as TEXT) ~* '.*${text}.*'" +
          s"  OR CAST(partnumber as TEXT) ~* '.*${text}.*' " +
          s"OR CAST(model as TEXT) ~* '.*${text}.*'" +
          s")"
      }
      else ""

    }.getOrElse {
      ""
    }
  }

  private def supplierWhere(supplierId: Option[Long]): String = {
    val start = " AND "
    supplierId.map { supplier =>
      start + s"supplier = ${supplier}"
    }.getOrElse {
      ""
    }
  }

}
