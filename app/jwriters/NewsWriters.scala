package jwriters

import models.{NewsText, News, ManufacturerText}
import play.api.libs.json.{Writes, _}
import repositories.{NewsEdit, ManufacturerEdit}


object NewsWriters {
  implicit val newsWrites = new Writes[News] {
    def writes(news: News) = Json.obj(
      "id" -> news.id,
      "havePicture" -> news.picture.nonEmpty
    )
  }

  implicit val newsTextWrites = new Writes[NewsText] {
    def writes(newsText: NewsText) = Json.obj(
      "newsid" -> newsText.newsid,
      "languageid" -> newsText.languageid,
      "title" -> newsText.title,
      "content" -> newsText.content
    )
  }

  implicit val NewsEditWrites = new Writes[NewsEdit] {
    def writes(newsEdit: NewsEdit) = Json.obj(
      "news" -> Json.toJson(newsEdit.news),
      "pictureUrl" -> Json.toJson(newsEdit.pictureUrl),
      "data" -> Json.toJson(
        newsEdit.data.toMap
      )
    )
  }
}
