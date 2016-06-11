package jwriters

import models.Picture
import play.api.libs.json.{Writes, _}


object PictureWriters {
  implicit val pictureWrites = new Writes[Picture] {
    def writes(picture: Picture) = Json.obj(
      "id" -> picture.id,
      "data" -> picture.data,
      "extension" -> picture.extension
    )
  }

}

