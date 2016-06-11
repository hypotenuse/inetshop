package models

import scalikejdbc._

case class NewsText(
  languageid: Long,
  newsid: Long,
  title: String,
  content: Option[String] = None) {

  def save()(implicit session: DBSession = NewsText.autoSession): NewsText = NewsText.save(this)(session)

  def destroy()(implicit session: DBSession = NewsText.autoSession): Unit = NewsText.destroy(this)(session)

}


object NewsText extends SQLSyntaxSupport[NewsText] {

  override val tableName = "news_texts"

  override val columns = Seq("languageid", "newsid", "title", "content")

  def apply(nt: SyntaxProvider[NewsText])(rs: WrappedResultSet): NewsText = apply(nt.resultName)(rs)
  def apply(nt: ResultName[NewsText])(rs: WrappedResultSet): NewsText = new NewsText(
    languageid = rs.get(nt.languageid),
    newsid = rs.get(nt.newsid),
    title = rs.get(nt.title),
    content = rs.get(nt.content)
  )

  val nt = NewsText.syntax("nt")

  override val autoSession = AutoSession

  def find(languageid: Long, newsid: Long)(implicit session: DBSession = autoSession): Option[NewsText] = {
    withSQL {
      select.from(NewsText as nt).where.eq(nt.languageid, languageid).and.eq(nt.newsid, newsid)
    }.map(NewsText(nt.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[NewsText] = {
    withSQL(select.from(NewsText as nt)).map(NewsText(nt.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(NewsText as nt)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[NewsText] = {
    withSQL {
      select.from(NewsText as nt).where.append(where)
    }.map(NewsText(nt.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[NewsText] = {
    withSQL {
      select.from(NewsText as nt).where.append(where)
    }.map(NewsText(nt.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(NewsText as nt).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    languageid: Long,
    newsid: Long,
    title: String,
    content: Option[String] = None)(implicit session: DBSession = autoSession): NewsText = {
    withSQL {
      insert.into(NewsText).columns(
        column.languageid,
        column.newsid,
        column.title,
        column.content
      ).values(
        languageid,
        newsid,
        title,
        content
      )
    }.update.apply()

    NewsText(
      languageid = languageid,
      newsid = newsid,
      title = title,
      content = content)
  }

  def save(entity: NewsText)(implicit session: DBSession = autoSession): NewsText = {
    withSQL {
      update(NewsText).set(
        column.languageid -> entity.languageid,
        column.newsid -> entity.newsid,
        column.title -> entity.title,
        column.content -> entity.content
      ).where.eq(column.languageid, entity.languageid).and.eq(column.newsid, entity.newsid)
    }.update.apply()
    entity
  }

  def destroy(entity: NewsText)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(NewsText).where.eq(column.languageid, entity.languageid).and.eq(column.newsid, entity.newsid) }.update.apply()
  }

}
