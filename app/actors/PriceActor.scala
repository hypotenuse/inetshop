package actors

import akka.actor.Actor
import models._
import scalikejdbc._

import scala.util.Try

class PriceActor extends Actor {

  def receive = {
    case "recalculate" =>
//      val g = Good.syntax("g")
//      DB readOnly { implicit session =>
//        withSQL {
//          select(g.id).from(Good as g)
//        }.foreach { rs =>
      DB readOnly { implicit session =>
        sql"select id from goods".foreach { rs =>
          val id = rs.long("id")
          val minCost: Option[(SupplierPrice,BigDecimal)] = Try(
            SupplierPrice.findBy(sqls"good = $id").map { g =>
              (g, convertCostToMainCurrency(g.supplier, g.cost, g.currency))
            }.minBy(_._2)
          ).toOption

          for {
            (priceGood, c) <- minCost
            good <- Good.find(id)
            newCost <- calculateCost(c)
          } good.copy(cost = newCost, supplierid = Some(priceGood.supplier), suppliercost = Some(c)).save()

        }
      }
      sender() ! "recalculated"
  }

  private def convertCostToMainCurrency(supplierId: Long, cost: BigDecimal, currencyId: Long): BigDecimal = {
    val supplierRate = SupplierCurrencyRate.find(supplierId, currencyId).map(_.rate)
    Currency.find(currencyId).map { c =>
      if (c.main) cost else{
        supplierRate.map{r =>
          cost * r
        }.getOrElse(cost * c.cros)
      }
    }.get
  }

  def calculateCost(supplierCost: BigDecimal): Option[BigDecimal]= {
    CostScale.findAll().find{scale =>
      supplierCost <= scale.finish && supplierCost >= scale.start
    }.map{scale =>
      supplierCost * scale.percent / 100 + supplierCost
    }
  }
}
