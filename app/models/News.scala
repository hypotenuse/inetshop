package models

import org.joda.time.DateTime
import scalikejdbc._
import services.SlugifyService
import traits.{HaveThumb, HavePicture}
import validators.{JNewsUpdate, JNewsNew}

case class News(
  id: Long,
  picture: Option[Array[Byte]] = None,
  extension: Option[String] = None,
  slug: String,
  createdat: DateTime) extends HavePicture with HaveThumb{
  val pictureDir = "news"

  def save()(implicit session: DBSession = News.autoSession): News = News.save(this)(session)

  def pictureUrl(baseUrl: String): String = News.pictureUrl(baseUrl, this)

  def pictureThumbUrl(baseUrl: String): String = News.pictureThumbUrl(baseUrl, this)

  def destroy()(implicit session: DBSession = News.autoSession): Unit = News.destroy(this)(session)

  def textByDefaultLang(implicit session: DBSession = News.autoSession): Option[NewsText] = News.getTextByDefaultLang(this)(session)

  def textByLang(lang: Language)(implicit session: DBSession = News.autoSession): Option[NewsText] = News.getTextByLang(this, lang)(session)

  def pictureExtension : String = News.pictureExtension(this)
}


object News extends SQLSyntaxSupport[News] {

  override val tableName = "news"

  override val columns = Seq("id", "picture", "extension", "slug", "createdat")

  def apply(n: SyntaxProvider[News])(rs: WrappedResultSet): News = apply(n.resultName)(rs)
  def apply(n: ResultName[News])(rs: WrappedResultSet): News = new News(
    id = rs.get(n.id),
    picture = rs.get(n.picture),
    extension = rs.get(n.extension),
    slug = rs.get(n.slug),
    createdat = rs.get(n.createdat)
  )

  val n = News.syntax("n")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[News] = {
    withSQL {
      select.from(News as n).where.eq(n.id, id)
    }.map(News(n.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[News] = {
    withSQL(select.from(News as n)).map(News(n.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(News as n)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[News] = {
    withSQL {
      select.from(News as n).where.append(where)
    }.map(News(n.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[News] = {
    withSQL {
      select.from(News as n).where.append(where)
    }.map(News(n.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(News as n).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    picture: Option[Array[Byte]] = None,
    extension: Option[String] = None,
    slug: String)(implicit session: DBSession = autoSession): News = {
    val time: DateTime = DateTime.now()
    val generatedKey = withSQL {
      insert.into(News).columns(
        column.picture,
        column.extension,
        column.slug,
        column.createdat
      ).values(
        picture,
        extension,
        slug,
        time
      )
    }.updateAndReturnGeneratedKey.apply()

    News(
      id = generatedKey,
      picture = picture,
      extension = extension,
      slug = slug,
      createdat = time
    )
  }

  def save(entity: News)(implicit session: DBSession = autoSession): News = {
    withSQL {
      update(News).set(
        column.id -> entity.id,
        column.picture -> entity.picture,
        column.extension -> entity.extension,
        column.slug -> entity.slug,
        column.createdat -> entity.createdat
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: News)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(News).where.eq(column.id, entity.id) }.update.apply()
    entity.deletePicture()
    entity.deleteThumb()
  }

  def create(jNews: JNewsNew): News ={
    val news = create(
      picture = None,
      extension = None,
      slug = SlugifyService.slugify(jNews.title)

    )
    Language.findBy(sqls"defaultlng = true").map{
      l =>
        NewsText.create(newsid = news.id, languageid = l.id, title = jNews.title)
    }.getOrElse{
      throw new Exception("Can't find default language in database!")
    }

    news
  }

  def save(newsId: Long, jNews: JNewsUpdate): News ={
    val maybeNews = News.find(newsId)
    val lang = Language.getByCod(jNews.languagecod).getOrElse{
      throw new Exception("Can't find language with code ${jNews.languagecod} in database!")}

    maybeNews.map{
      news =>
        NewsText.find(lang.id, news.id).map{
          t =>
            t.copy(
              title = jNews.title.getOrElse(t.title),
              content = jNews.content.orElse(t.content)
            ).save()

        }.getOrElse{
          jNews.title.map{
            t =>
              NewsText.create(
                newsid = news.id,
                languageid = lang.id,
                title = t,
                content = jNews.content
              )
          }.getOrElse(throw new IllegalArgumentException(s"Can't create text for language ${lang.cod} with empty title"))

        }
        news

    }.getOrElse(throw new NoSuchElementException("News does not exist"))

  }

  def getTextByDefaultLang(entity: News)(implicit session: DBSession = autoSession): Option[NewsText]={
    for{
      lan <- Language.findBy(sqls"defaultlng = TRUE")
      text <- NewsText.find(lan.id, entity.id)
    } yield text
  }

  def getTextByLang(entity: News, lang: Language)(implicit session: DBSession = autoSession): Option[NewsText]={
    NewsText.find(lang.id, entity.id)
  }

  def pictureUrl(baseUrl: String, entity: News): String = {
    entity.extension.map(e => baseUrl + entity.pictureDir + "/pictures/" + entity.id + "." + e)
      .getOrElse(baseUrl + entity.pictureDir + "/pictures/" + entity.id + "." + "jpg")
  }

  def pictureExtension(news: News): String ={
    news.extension.getOrElse("jpg")
  }

  def pictureThumbUrl(baseUrl: String, entity: News): String = {
    baseUrl + "thumbs/" + entity.pictureDir + "/" + entity.id + "." + "jpg"
  }

  def findLast(number: Int)(implicit session: DBSession = autoSession): List[News] = {
    withSQL {
      select.from(News as n).orderBy(sqls"createdat").limit(number)
    }.map(News(n.resultName)).list.apply()
  }

}
