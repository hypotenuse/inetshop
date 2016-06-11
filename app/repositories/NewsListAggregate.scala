package repositories

import models.{News}

case class NewsListAggregate(id: Long, title: String)

object NewsListAggregate {

  def list(): List[NewsListAggregate] = {
    News.findAll().flatMap{
      news=>
        news.textByDefaultLang.map(t => NewsListAggregate(news.id, t.title))
    }
  }

}
