package actors

import java.io.FileInputStream
import java.io.File
import akka.actor.Actor
import messages.{ExcelString, ExcelResult, NewPrice}
import models.ExcelSetting
import org.apache.commons.io.FilenameUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import play.api.libs.json.{Reads, JsPath, Json}
import validators.{CurrencyIdSign, CategoryColumn}
import play.api.libs.functional.syntax._

import scala.util.Try

class ExcelActor extends Actor {
  override def preStart(): Unit = {
//    println("Starting Excel actor")
  }

  def receive = {
    case price: NewPrice => processPrice(price)
  }

  private def processPrice(price: NewPrice) = {
//    println("At Excel")
    import play.api.Play.current


    val dir = current.path + current.configuration.getString("prices.path").get
    val extension = FilenameUtils.getExtension(price.filename)
    val file = new FileInputStream(new File(dir + price.filename))
    val workbook = extension match {
      case "xls" =>
        new HSSFWorkbook(file)
      case "xlsx" =>
        new XSSFWorkbook(file)
      case _ => throw new IllegalArgumentException("Price extension incorrect")
    }

    ExcelSetting.find(price.settingId).map { setting =>
      val sheet = workbook.getSheetAt(setting.sheetNumber)
      val rowIterator = sheet.iterator()
      rowIterator.next()
      while (rowIterator.hasNext) {
        val row = rowIterator.next()
        val title = Try(getCellValue(row.getCell(setting.titleColumn)).toString)
        val cost = Try(getCellValue(row.getCell(setting.costColumn)).toString.toDouble).toOption
        val available = Try(getCellValue(row.getCell(setting.amountColumn)).toString).toOption
        val costRrz = Try(setting.costRrzColumn.map(c => BigDecimal(getCellValue(row.getCell(c)).toString.toDouble))).toOption.flatten
        val partnumber = Try(setting.partnumberColumn.map(c => getCellValue(row.getCell(c)).toString)).toOption.flatten
        val warranty = parseWarranty(setting, row)
        val supplierGoodid = Try(setting.goodidSupplierColumn.map(c => getCellValue(row.getCell(c)).toString)).toOption.flatten
        val brand = Try(setting.brandColumn.map(c => getCellValue(row.getCell(c)).toString)).toOption.flatten
        val category = setting.categoryColumns.flatMap(c => parseCategory(row, c))
        val description = Try(setting.descriptionColumn.map(c => getCellValue(row.getCell(c)).toString)).toOption.flatten
        val imageUrl = Try(setting.imageUrlColumn.map(c => getCellValue(row.getCell(c)).toString)).toOption.flatten
        val model = Try(setting.modelColumn.map(c => getCellValue(row.getCell(c)).toString)).toOption.flatten
        for {
          t <- title
          c <- cost
          currency <- getCurrencyId(setting = setting, row = row)
          if isAvailable(available = available, cost = cost, title = t)
        } {
          val result = ExcelResult(
            supplierId = price.supplierId,
            settingId = price.settingId,
            filename = price.filename,
            currencyId = currency,
            excelString = ExcelString(
              title = t,
              cost = c,
              costRrz = costRrz,
              partnumber = partnumber,
              warranty = warranty,
              brand = brand,
              supplierGoodid = supplierGoodid,
              category = category,
              description = description,
              imageUrl = imageUrl,
              model = model
            ))

          sender() ! result
        }
      }
      file.close()
      sender() ! "ExcelFinish"
      setting
    }.getOrElse(println("Can't find setting"))


  }

  private def getCellValue(cell: Cell) = {
    cell.getCellType match {
      case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue
      case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue
      case Cell.CELL_TYPE_STRING => cell.getStringCellValue
    }
  }

  private def parseCategory(row: org.apache.poi.ss.usermodel.Row, categories: String): Option[String] = {
    import jreaders.ExcelSettingReaders.categoryColumnsReads
    val cats = Json.parse(categories).asOpt[List[CategoryColumn]]
    val catsContent: Option[List[Option[String]]] =
      cats.map { l =>
        l.map { c =>
          Try(getCellValue(row.getCell(c.column)).asInstanceOf[String]).toOption
        }
      }
    catsContent.map { l =>
      l.filter { v => v.nonEmpty }.map{_.get}.mkString(" \\ ")
    }
  }

  private def parseWarranty(setting: ExcelSetting, row: org.apache.poi.ss.usermodel.Row): Option[Int] = {
    val fieldValue: Option[String] = Try(setting.warrantyColumn.map(c => getCellValue(row.getCell(c)).toString)).toOption.flatten
    Try(fieldValue.map{v =>
      val valueParsed = v.replace(".0", "").replaceAll("[\\D]", "").toInt
      setting.warrantyInMonths match{
        case Some(n) if n =>  valueParsed
        case Some(n) if !n => valueParsed * 12
        case None => valueParsed * 12
      }
    }).toOption.flatten
  }

  private def getCurrencyId(setting: ExcelSetting, row: org.apache.poi.ss.usermodel.Row): Option[Long] = {
    implicit val currencyIdSignReads: Reads[CurrencyIdSign] = (
    (JsPath \ "currency").read[Long] and
    (JsPath \ "sign").read[String]
    )(CurrencyIdSign.apply _)
    val result = setting.costCurrencyId.orElse {
      val signsData = for {
        curSignColumn <- setting.currencySignColumn
        signs <- setting.currencyIdSign
        currentSign <- Try(getCellValue(row.getCell(curSignColumn)).asInstanceOf[String]).toOption
        currencyIdSigns <- Json.parse(signs).asOpt[List[CurrencyIdSign]]
      } yield (currentSign, currencyIdSigns)


      signsData.flatMap { case (currentSign, currencyIdSigns) =>
        val currency: List[CurrencyIdSign] = currencyIdSigns.filter(_.sign == currentSign)
        Try(currency.head.currency).toOption
      }

    }
    result
  }

  private def isAvailable(available: Option[String], cost: Option[Double], title: String): Boolean = {
    val goodAvailableSigns = List("Есть", "есть")

    val avail: Option[(String, Double)] = for {
      a <- available
      c <- cost
      if c > 0
    } yield (a, c)

    avail.exists { case (av, cst) =>
      av match {
        case _ if Try(av.replaceAll("[\\W]", "").toInt).toOption.nonEmpty =>
          val cleaned = av.replaceAll("[\\W]", "")
          val count = Try(cleaned.toInt).toOption
          count.exists(_ > 0)
        case _ =>
          val cleaned = av.replaceAll("[\\s]+", "")
          goodAvailableSigns.exists(v => cleaned.compareTo(v) == 0)
      }
    }
  }
}
