package repositories

import models.Slide


case class SlidesListAggregate(id: Long, url: Option[String], picture: String)

object SlidesListAggregate {

  def list(baseUrl: String): List[SlidesListAggregate] = {
    Slide.findAll().map{
      s=>
        SlidesListAggregate(s.id, s.url, s.thumbUrl(baseUrl))
    }
  }

}
