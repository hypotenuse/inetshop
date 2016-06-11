package repositories

import models._
import play.api.mvc.RequestHeader
import scalikejdbc._
import services.UrlService

case class SupplierEdit(supplier: Supplier, currencies: List[SupplierCurrency])
case class SupplierCurrency(id: Long, title: String, rate: Option[BigDecimal])

object SupplierEditAggregate {
  def get(id: Long): Option[SupplierEdit] = {
    implicit val session = AutoSession
    val maybeSupplier = Supplier.find(id)
    maybeSupplier.map {
      supplier =>
        val currencies = for {
          c <- Currency.findAll().filter(c => !c.main)
        } yield SupplierCurrency(c.id, c.title, SupplierCurrencyRate.find(supplier = supplier.id, currency = c.id).map(s => s.rate)orElse(None))

        Some(SupplierEdit(supplier, currencies))
    }.getOrElse(None)
  }

}
