package repositories

import models.{Sale, ManufacturerText, Language, Good, Manufacturer}
import play.Logger
import scalikejdbc._
import services.Repository

import scala.util.{Failure, Success}


object GoodSearch {
  implicit val session = AutoSession

  val what =
    """goods.id,
                  good_texts.title,
                  partnumber,
                  manufacturers.id as manufacturerid,
                  cost,
                  suppliercost,
                  warranty,
                  supplierid,
                  suppliers.title as supplier,
                  import_short_desc"""

  val whatWithoutText =
    """goods.id,
                  partnumber,
                  manufacturers.id as manufacturerid,
                  cost,
                  warranty,
                  supplierid,
                  suppliers.title as supplier,
                  import_short_desc"""

  def getList(
               languageid: Long,
               length: Int,
               offset: Int,
               order: String,
               orderColumn: String,
               searchText: Option[String] = None,
               manufacturerId: Option[Long],
               categoryId: Option[Long],
               costTo: Option[Int],
               costFrom: Option[Int]
               ): (List[GoodListAggregate], Int, Int) = {
    val addFromCategory = categoryId.map(id => Some(filterByCategoryFrom(id))).getOrElse(None)
    val addWhereCategory = categoryId.map(id => filterByCategoryWhere(id)).getOrElse("")
    val addWhereManufacturer = manufacturerId.map(id => filterByManufacturerWhere(id)).getOrElse("")
    val addWhereCostFrom = costFrom.map(cost => filterByCostFromWhere(cost)).getOrElse("")
    val addWhereCostTo = costTo.map(cost => filterByCostToWhere(cost)).getOrElse("")
    val allWhere = addWhereCategory + addWhereManufacturer + addWhereCostFrom + addWhereCostTo
    (
      search(languageid, length, offset, order, orderColumn, searchText, addFromCategory, allWhere),
      count(languageid, searchText, addFromCategory, allWhere).getOrElse(0),
      count(languageid, None).getOrElse(0)
      )
  }

  def getListNotInSale(
                        sale: Sale,
                        languageid: Long,
                        length: Int,
                        offset: Int,
                        order: String,
                        orderColumn: String,
                        searchText: Option[String] = None,
                        manufacturerId: Option[Long],
                        categoryId: Option[Long],
                        costTo: Option[Int],
                        costFrom: Option[Int]
                        ): (List[GoodListAggregate], Int, Int) = {
    val addFromCategory = categoryId.map(id => Some(filterByCategoryFrom(id))).getOrElse(None)
    val addWhereCategory = categoryId.map(id => filterByCategoryWhere(id)).getOrElse("")
    val addWhereManufacturer = manufacturerId.map(id => filterByManufacturerWhere(id)).getOrElse("")
    val addWhereCostFrom = costFrom.map(cost => filterByCostFromWhere(cost)).getOrElse("")
    val addWhereCostTo = costTo.map(cost => filterByCostToWhere(cost)).getOrElse("")
    val allWhere = addWhereCategory + addWhereManufacturer + addWhereCostFrom + addWhereCostTo
    val allGoodList= search(languageid, 1000000, 0, order, orderColumn, searchText, addFromCategory, allWhere)
    val allCountFiltered = count(languageid, searchText, addFromCategory, allWhere).getOrElse(0)
    val allCountTotal = count(languageid, None).getOrElse(0)
    val (goodsInSale, inSaleFiltered, inSaleTotal) = getListInSale(sale.id, languageid, 1000000, 0, order, orderColumn, None, None, None, None, None)
    val notInSaleGoods = allGoodList.filterNot(g => goodsInSale.exists(ag => ag.id == g.id))
    val (_, notInSaleGoodsPaged) = notInSaleGoods.splitAt(offset)
    val notInSaleGoodsLimited = notInSaleGoodsPaged.take(length)
    val countNotInSaleFiltered = allCountFiltered - inSaleFiltered
    val countNotInSaleTotal = allCountTotal - inSaleTotal
    (notInSaleGoodsLimited, countNotInSaleFiltered, countNotInSaleTotal)
  }

  def getListInSale(
                     saleId: Long,
                     languageid: Long,
                     length: Int,
                     offset: Int,
                     order: String,
                     orderColumn: String,
                     searchText: Option[String] = None,
                     manufacturerId: Option[Long],
                     categoryId: Option[Long],
                     costTo: Option[Int],
                     costFrom: Option[Int]
                     ): (List[GoodListAggregate], Int, Int) = {
    val addFromCategory: Option[String] = categoryId.map(id => Some(filterByCategoryFrom(id))).getOrElse(None)
    val addWhereCategory = categoryId.map(id => filterByCategoryWhere(id)).getOrElse("")
    val addWhereManufacturer = manufacturerId.map(id => filterByManufacturerWhere(id)).getOrElse("")
    val addWhereCostFrom = costFrom.map(cost => filterByCostFromWhere(cost)).getOrElse("")
    val addWhereCostTo = costTo.map(cost => filterByCostToWhere(cost)).getOrElse("")
    val addFromSale: String = filterBySaleFrom(saleId)
    val addFilterBySaleWhere = filterBySaleWhere(saleId)
    val allWhere = addFilterBySaleWhere + addWhereCategory + addWhereManufacturer + addWhereCostFrom + addWhereCostTo
    val allFrom: Option[String] = addFromCategory.map(f => addFromSale + " " + f).orElse(Some(addFromSale))
    (
      search(languageid, length, offset, order, orderColumn, searchText, allFrom, allWhere),
      count(languageid, searchText, allFrom, allWhere).getOrElse(0),
      count(languageid, None, Some(addFromSale), addFilterBySaleWhere).getOrElse(0)
      )
  }


  private def formSearchWhere(searchText: Option[String]) = {
    if (searchText.isDefined) {
      //      val cond = LikeConditionEscapeUtil.contains(searchText.get)
      s" and (CAST(goods.id as TEXT) ~* '.*${searchText.get}.*'" +
        s"  OR CAST(partnumber as TEXT) ~* '.*${searchText.get}.*' " +
        s"OR CAST(good_texts.title as TEXT) ~* '.*${searchText.get}.*'" +
        s")"
    }
    else {
      ""
    }
  }

  private def from(additional: Option[String] = None): String = {
    val initial =
      """goods
                      JOIN good_texts ON goods. ID = good_texts.goodid
                      LEFT JOIN manufacturers ON manufacturers.id = goods.manufacturer
                      LEFT JOIN suppliers on suppliers.id = goods.supplierid"""

    additional.map { ad =>
      initial + " " + ad
    }.getOrElse(
      initial
    )
  }


  private def count(languageid: Long, searchText: Option[String], additionalFrom: Option[String] = None, additionalWhere: String = "") = {
    Repository.count(from(additionalFrom), s"good_texts.languageid = ${languageid}${formSearchWhere(searchText)}${additionalWhere}") match {
      case Success(v) => v
      case Failure(t) => throw new RuntimeException(t)
    }
  }

  private def search(languageid: Long, length: Int, offset: Int, order: String, orderColumn: String, searchText: Option[String] = None, additionalFrom: Option[String] = None, additionalWhere: String = ""): List[GoodListAggregate] = {
    val dbResult = Repository.getData(from(additionalFrom), what, s"languageid = ${languageid}${formSearchWhere(searchText)}${additionalWhere}", orderColumn + " " + order, length, offset)
    dbResult.map {
      rs =>
        GoodListAggregate(
          id = rs.long("id"),
          title = rs.string("title"),
          partnumber = rs.stringOpt("partnumber"),
          manufacturerid = rs.longOpt("manufacturerid"),
          manufacturerTitle = rs.longOpt("manufacturerid").flatMap { id =>
            ManufacturerText.find(languageid, id).map(t => t.title)
          },
          cost = rs.bigDecimalOpt("cost"),
          supplierСost = rs.bigDecimalOpt("suppliercost"),
          warranty = rs.intOpt("warranty"),
          supplierid = rs.longOpt("supplierid"),
          supplier = rs.stringOpt("supplier"),
          import_short_desc = rs.boolean("import_short_desc")
        )
    }.list().apply()
  }

  private def filterByCategoryFrom(categoryId: Long): String = {
    "JOIN good_category ON goods.id = good_category.goodid"
  }

  private def filterBySaleFrom(saleId: Long): String = {
    "JOIN sale_good ON goods.id = sale_good.goodid"
  }

  private def filterBySaleWhere(saleId: Long): String = {
    s" and sale_good.saleid = ${saleId}"
  }

  private def filterByCategoryWhere(categoryId: Long): String = {
    s" and good_category.categoryid = ${categoryId}"
  }

  private def filterByManufacturerWhere(manufacturerId: Long): String = {
    s" and manufacturers.id = ${manufacturerId}"
  }

  private def filterByCostFromWhere(costFrom: Int): String = {
    s" and cost >= ${costFrom}"
  }

  private def filterByCostToWhere(costTo: Int): String = {
    s" and cost <= ${costTo}"
  }

  def frontendAggregate(langv: Language, list: List[Good]): List[GoodFrontendAggregate] = {
    list.map{g =>
      val text = g.textByLang(langv.id).orElse(g.textByLang(models.Language.getDefault.get.id))
      val mtitle: Option[String] = for {
        m <- g.manufacturer
        manufacturer <- Manufacturer.find(m)
        mtext <- manufacturer.textByLang(langv.id).orElse(manufacturer.textByDefaultLang)
      } yield mtext.title

      val picture = g.picturesWithoutData.map{_._1}.headOption

      text.map{t =>
        GoodFrontendAggregate(
          id = g.id,
          title = t.title,
          partnumber = g.partnumber,
          manufacturerTitle = mtitle,
          cost = g.cost,
          warranty = g.warranty,
          short_desc = t.descriptionShort,
          picture = picture,
          slug = g.slug
        )
      }.get

    }
  }
}
