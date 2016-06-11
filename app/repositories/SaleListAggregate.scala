package repositories

import models.Sale

case class SaleListAggregate(id: Long, title: String)

object SaleListAggregate {

  def list(): List[SaleListAggregate] = {
    Sale.findAll().flatMap{
      sale=>
        sale.textByDefaultLang.map(t => SaleListAggregate(sale.id, t.title))
    }
  }

}


