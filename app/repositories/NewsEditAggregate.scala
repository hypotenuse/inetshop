package repositories

import models.{NewsText, Language, News}
import play.api.mvc.RequestHeader
import scalikejdbc._
import services.UrlService

case class NewsEdit(news: News, pictureUrl: String, data: List[(String, Option[NewsText])])
object NewsEditAggregate {
  def get(id: Long, request: RequestHeader): Option[NewsEdit]={
    implicit val session = AutoSession
    val news = News.find(id)
    news.map{
      n =>
        val newsTextData: List[(String, Option[NewsText])] = for(lang <- Language.findAll()) yield (lang.cod, NewsText.findBy(sqls"languageid = ${lang.id} and newsid = ${id}"))
        Some(NewsEdit(n, n.pictureThumbUrl(UrlService.baseUrl(request)), newsTextData))
    }.getOrElse(None)
  }

}
