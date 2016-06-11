package jwriters

import play.api.libs.json.{Writes, _}
import repositories.NewsListAggregate


object NewsListAggregateWriters {
  implicit val newsWrites = new Writes[NewsListAggregate] {
    def writes(news: NewsListAggregate) =
      JsArray(
        Seq(
          JsNumber(news.id),
          JsString(news.title)
        )
      )
  }

}

