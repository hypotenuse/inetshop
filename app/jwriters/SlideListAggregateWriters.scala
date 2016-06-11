package jwriters

import play.api.libs.json.{Writes, _}
import repositories.SlidesListAggregate


object SlideListAggregateWriters {
  implicit val slideWrites = new Writes[SlidesListAggregate] {
    def writes(slide: SlidesListAggregate) = Json.obj(
      "id" -> JsNumber(slide.id),
      "url" -> JsString(slide.url.getOrElse("/")),
      "picture" -> JsString(slide.picture)
    )
  }

}

