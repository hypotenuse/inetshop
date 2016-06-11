package actors

import akka.actor.Actor
import messages.{PriceUpdated, UpdatePrice, CleanData, ExcelResult}
import models.{SupplierGoodsMap, SupplierPrice, ExcelSetting, RawSupplierGood}
import scalikejdbc._

class DbActor extends Actor {
  override def preStart(): Unit = {
    //    println("Starting DB actor")
  }

  def receive = {
    case result: ExcelResult =>
      ExcelSetting.find(result.settingId).foreach { setting =>
        //        println(result.toString)
        RawSupplierGood.create(
          supplier = result.supplierId,
          title = result.excelString.title,
          cost = result.excelString.cost,
          costCurrencyId = result.currencyId,
          costRrzCurrencyId = setting.costRrzCurrencyId,
          costRrz = result.excelString.costRrz,
          partnumber = result.excelString.partnumber,
          warranty = result.excelString.warranty,
          brand = result.excelString.brand,
          supplierGoodid = result.excelString.supplierGoodid,
          category = result.excelString.category,
          description = result.excelString.description,
          imageUrl = result.excelString.imageUrl,
          model = result.excelString.model
        )
      }

    case clean: CleanData =>
      DB localTx { implicit session =>
        //        sql"TRUNCATE raw_suppliers_goods RESTART IDENTITY".update.apply()
        sql"UPDATE raw_suppliers_goods SET id=1000000 + nextval('raw_suppliers_goods_id_seq')".update.apply()
        sql"ALTER SEQUENCE raw_suppliers_goods_id_seq RESTART WITH 1".update.apply()
        sql"UPDATE raw_suppliers_goods SET id=nextval('raw_suppliers_goods_id_seq')".update.apply()
        withSQL {
          delete.from(RawSupplierGood).where.eq(sqls"supplier", clean.supplierId)
        }.update.apply()
      }

    case "PriceFinish" =>
      sender() ! "PriceFinish"

    case update: UpdatePrice =>
      println(update)
      println("What before: " + SupplierPrice.findAll())
      DB localTx { implicit session =>
        withSQL {
          delete.from(SupplierPrice).where(sqls"supplier = ${update.supplierId}")
        }.update.apply()
      }
      println("What after: " + SupplierPrice.findAll())

      val rawGoods: List[RawSupplierGood] = RawSupplierGood.findAllBy(sqls"supplier = ${update.supplierId}")
      val dataToInsert: List[(RawSupplierGood, Long)] = for {
        rg <- rawGoods
        map <- SupplierGoodsMap.findBy(sqls"text = ${rg.text} and supplier = ${update.supplierId}")
      } yield (rg, map.good)

      if (dataToInsert.length > 1) throw new Exception("More than one good for md5 text " + dataToInsert.head._1.text)
      else {
        dataToInsert.map { case (raw, id) =>
          println("Adding")
          SupplierPrice.create(
            updated = raw.updated,
            good = id,
            supplier = raw.supplier,
            cost = raw.cost,
            currency = raw.costCurrencyId,
            costRrz = raw.costRrz,
            costRrzCurrencyId = raw.costRrzCurrencyId
          )
        }
      }


      sender() ! PriceUpdated(update.supplierId, update.file)
  }


}
