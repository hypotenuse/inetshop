package repositories

import models.Category

case class CategorySelectAggregate(id: Long, title: String, pathtoroot: String )
object CategorySelectAggregate {
  def list(category: Option[Category] = None): List[CategorySelectAggregate] = {
    val agregates = Category.findAll().map {
      cat =>
        val t = cat.textByDefaultLang.get
        CategorySelectAggregate(cat.id, t.title, t.pathtoroot)
    }
    category.map{
      c =>
        agregates.filterNot(a => c.childrenIds().contains(a.id)).filterNot(a => a.id == c.id)
    }.getOrElse(agregates)
  }
}
