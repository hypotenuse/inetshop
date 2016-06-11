package models

import play.Logger
import play.api.libs.json.Json
import scalikejdbc._

import scala.annotation.tailrec

case class CategoryText(
  catid: Long,
  title: String,
  description: Option[String] = None,
  metatitle: Option[String] = None,
  metadescription: Option[String] = None,
  pathtoroot: String,
  languageid: Long) {

  def save()(implicit session: DBSession = CategoryText.autoSession): CategoryText = CategoryText.save(this)(session)

  def destroy()(implicit session: DBSession = CategoryText.autoSession): Unit = CategoryText.destroy(this)(session)

}



object CategoryText extends SQLSyntaxSupport[CategoryText] {

  override val tableName = "category_texts"

  override val columns = Seq("catid", "title", "description", "metatitle", "metadescription", "pathtoroot", "languageid")

  def apply(ct: SyntaxProvider[CategoryText])(rs: WrappedResultSet): CategoryText = apply(ct.resultName)(rs)
  def apply(ct: ResultName[CategoryText])(rs: WrappedResultSet): CategoryText = new CategoryText(
    catid = rs.get(ct.catid),
    title = rs.get(ct.title),
    description = rs.get(ct.description),
    metatitle = rs.get(ct.metatitle),
    metadescription = rs.get(ct.metadescription),
    pathtoroot = rs.get(ct.pathtoroot),
    languageid = rs.get(ct.languageid)
  )

  val ct = CategoryText.syntax("ct")

  override val autoSession = AutoSession

  def find(languageid: Long, catid: Long)(implicit session: DBSession = autoSession): Option[CategoryText] = {
    withSQL {
      select.from(CategoryText as ct).where.eq(ct.languageid, languageid).and.eq(ct.catid, catid)
    }.map(CategoryText(ct.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[CategoryText] = {
    withSQL(select.from(CategoryText as ct)).map(CategoryText(ct.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(CategoryText as ct)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[CategoryText] = {
    withSQL {
      select.from(CategoryText as ct).where.append(where)
    }.map(CategoryText(ct.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[CategoryText] = {
    withSQL {
      select.from(CategoryText as ct).where.append(where)
    }.map(CategoryText(ct.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(CategoryText as ct).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              catid: Long,
              languageid: Long,
              title: String,
              description: Option[String] = None,
              metatitle: Option[String] = None,
              metadescription: Option[String] = None
              )(implicit session: DBSession = autoSession): CategoryText = {
    val pathtoroot : String = pathToRoot(catid, title, languageid)
    Logger.debug(pathtoroot)
    withSQL {
      insert.into(CategoryText).columns(
        column.catid,
        column.title,
        column.description,
        column.metatitle,
        column.metadescription,
        column.pathtoroot,
        column.languageid
      ).values(
        catid,
        title,
        description,
        metatitle,
        metadescription,
        pathtoroot,
        languageid
      )
    }.update().apply()

    CategoryText(
      catid = catid,
      title = title,
      description = description,
      metatitle = metatitle,
      metadescription = metadescription,
      pathtoroot = pathtoroot,
      languageid = languageid)
  }

  def save(entity: CategoryText)(implicit session: DBSession = autoSession): CategoryText = {
    withSQL {
      update(CategoryText).set(
        column.catid -> entity.catid,
        column.title -> entity.title,
        column.description -> entity.description,
        column.metatitle -> entity.metatitle,
        column.metadescription -> entity.metadescription,
        column.pathtoroot -> pathToRoot(entity.catid, entity.title, entity.languageid),
        column.languageid -> entity.languageid
      ).where.eq(column.languageid, entity.languageid).and.eq(column.catid, entity.catid)
    }.update.apply()
    entity
  }

  def destroy(entity: CategoryText)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(CategoryText).where.eq(column.languageid, entity.languageid).and.eq(column.catid, entity.catid) }.update.apply()
  }

  private def category(categoryText: CategoryText): Option[Category]={
    Category.find(categoryText.catid)
  }

  private def pathToRoot(catId: Long, title: String, ourLangId: Long):String ={
//    @tailrec
    def buildPath(list: List[Map[String, String]], catId: Long): List[Map[String, String]] =
    {
      val category = Category.find(catId).get
      if (category.parent.isDefined){
        val parent: Option[Category] = Category.find(category.parent.get).flatMap(c => Category.find(c.id))
        val parentText: Option[CategoryText] = Category.find(category.parent.get).flatMap(c => CategoryText.find(ourLangId, c.id))
        parentText match{
          case Some(p) => buildPath(Map("title" -> p.title, "slug" -> parent.get.slug) :: list, parent.get.id)
          case None =>
            val parentObject: Category = Category.find(category.parent.get).get
            val parentTextAnyLang = CategoryText.findAllBy(sqls"catid = ${parentObject.id}")
            Language.getDefault.map{
              l =>
                val textDefaultLanguage: List[CategoryText] = parentTextAnyLang.filter(t => t.languageid == l.id)
                if(textDefaultLanguage.nonEmpty){
                  buildPath(Map("title" -> textDefaultLanguage.head.title, "slug" -> parent.get.slug) :: list, parent.get.id)
                }
                else {
                  buildPath(Map("title" -> parentTextAnyLang.head.title, "slug" -> parent.get.slug) :: list, parent.get.id)
                }
            }getOrElse {
              buildPath(Map("title" -> parentTextAnyLang.head.title, "slug" -> parent.get.slug) :: list, parent.get.id)
            }
        }
      }
      else {
        list
      }
    }
    Json.stringify(Json.toJson(buildPath(List.empty, catId)))
  }

  def pathToRoot(categoryText: CategoryText) : Option[List[Map[String, String]]]={
    Json.parse(categoryText.pathtoroot).asOpt[List[Map[String, String]]]
  }

  def gebByLang(lang: Language): List[CategoryText] = {
    CategoryText.findAllBy(sqls"languageid=${lang.id}")
  }

  def gebByLangCod(cod: String): List[CategoryText] = {
    Language.findBy(sqls"cod=${cod}").map{
      l => CategoryText.gebByLang(l)
    }.getOrElse(List())
  }

}
