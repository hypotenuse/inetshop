package repositories

import models.{ManufacturerText, Language, Manufacturer}
import scalikejdbc._

case class ManufacturerEdit(manufacturer: Manufacturer, data: List[(String, Option[ManufacturerText])])
object ManufacturerEditAggregate {
  def get(id: Long): Option[ManufacturerEdit]={
    implicit val session = AutoSession
    val man = Manufacturer.find(id)
    man.map{
      manufacturer =>
        val manufacturerTextData: List[(String, Option[ManufacturerText])] = for(lang <- Language.findAll()) yield (lang.cod, ManufacturerText.findBy(sqls"languageid = ${lang.id} and manufacturerid = ${id}"))
        Some(ManufacturerEdit(manufacturer, manufacturerTextData))
    }.getOrElse(None)
  }

}
