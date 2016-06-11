package repositories

import models.{CategoryText, Language, Category}
import play.api.mvc.RequestHeader
import scalikejdbc._
import services.UrlService

case class CategoryEdit(category: Category, pictureUrl: String, pictureCategoryUrl: String, data: List[(String, Option[CategoryText])])
object CategoryEditAggregate {
  def get(id: Long, request: RequestHeader): Option[CategoryEdit]={
    implicit val session = AutoSession
    val cat = Category.find(id)
    cat.map{
      category =>
        val categoryTextData: List[(String, Option[CategoryText])] = for(lang <- Language.findAll()) yield (lang.cod, CategoryText.findBy(sqls"languageid = ${lang.id} and catid = ${id}"))
        Some(CategoryEdit(category, category.pictureUrl(UrlService.baseUrl(request)), category.pictureCategoryUrl(UrlService.baseUrl(request)), categoryTextData))
    }.getOrElse(None)
  }

}
