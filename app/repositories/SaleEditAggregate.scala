package repositories

import models.{SaleText, Language, Sale}
import scalikejdbc._

case class SaleEdit(sale: Sale, data: List[(String, Option[SaleText])])
object SaleEditAggregate {
  def get(id: Long): Option[SaleEdit]={
    implicit val session = AutoSession
    val sale = Sale.find(id)
    sale.map{
      sale =>
        val saleTextData: List[(String, Option[SaleText])] = for(lang <- Language.findAll()) yield (lang.cod, SaleText.findBy(sqls"languageid = ${lang.id} and saleid = ${id}"))
        Some(SaleEdit(sale, saleTextData))
    }.getOrElse(None)
  }

}
