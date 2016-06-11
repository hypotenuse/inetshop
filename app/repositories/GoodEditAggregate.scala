package repositories

import models.{Category, GoodText, Language, Good}
import scalikejdbc._

case class GoodEdit(good: Good, data: List[(String, Option[GoodText])], categories: List[CategorySelectAggregate])
object GoodEditAggregate {
  def get(id: Long): Option[GoodEdit]={
    def agregateCategories(cats: List[Category]): List[CategorySelectAggregate] = {
      cats.map{
        c =>
          val t = c.textByDefaultLang.get
          CategorySelectAggregate(c.id, t.title, t.pathtoroot)
      }
    }

    implicit val session = AutoSession
    val good = Good.find(id)
    good.map{
      g =>
        val goodTextData: List[(String, Option[GoodText])] = for(lang <- Language.findAll()) yield (lang.cod, GoodText.findBy(sqls"languageid = ${lang.id} and goodid = ${id}"))
        Some(GoodEdit(g, goodTextData, agregateCategories(g.categories())))
    }.getOrElse(None)
  }

}
