package jwriters

import models.{Manufacturer, ManufacturerText}
import play.api.libs.json.{Writes, _}
import repositories.ManufacturerEdit


object ManufacturerWriters {
  implicit val manufacturerWrites = new Writes[Manufacturer] {
    def writes(manufacturer: Manufacturer) = Json.obj(
      "id" -> manufacturer.id
    )
  }

  implicit val manufacturerTextWrites = new Writes[ManufacturerText] {
    def writes(manufacturerText: ManufacturerText) = Json.obj(
      "manufacturerid" -> manufacturerText.manufacturerid,
      "languageid" -> manufacturerText.languageid,
      "title" -> manufacturerText.title,
      "description" -> manufacturerText.description
    )
  }

  implicit val ManufacturerEditWrites = new Writes[ManufacturerEdit] {
    def writes(manufacturerEdit: ManufacturerEdit) = Json.obj(
      "manufacturer" -> Json.toJson(manufacturerEdit.manufacturer),
      "data" -> Json.toJson(
        manufacturerEdit.data.toMap
      )
    )
  }
}
