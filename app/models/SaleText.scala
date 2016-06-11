package models

import scalikejdbc._

case class SaleText(
  title: String,
  text: Option[String],
  languageid: Long,
  saleid: Long) {

  def save()(implicit session: DBSession = SaleText.autoSession): SaleText = SaleText.save(this)(session)

  def destroy()(implicit session: DBSession = SaleText.autoSession): Unit = SaleText.destroy(this)(session)

}


object SaleText extends SQLSyntaxSupport[SaleText] {

  override val tableName = "sales_texts"

  override val columns = Seq("title", "text", "languageid", "saleid")

  def apply(st: SyntaxProvider[SaleText])(rs: WrappedResultSet): SaleText = apply(st.resultName)(rs)
  def apply(st: ResultName[SaleText])(rs: WrappedResultSet): SaleText = new SaleText(
    title = rs.get(st.title),
    text = rs.get(st.text),
    languageid = rs.get(st.languageid),
    saleid = rs.get(st.saleid)
  )

  val st = SaleText.syntax("st")

  override val autoSession = AutoSession

  def find(languageid: Long, saleid: Long)(implicit session: DBSession = autoSession): Option[SaleText] = {
    withSQL {
      select.from(SaleText as st).where.eq(st.languageid, languageid).and.eq(st.saleid, saleid)
    }.map(SaleText(st.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[SaleText] = {
    withSQL(select.from(SaleText as st)).map(SaleText(st.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(SaleText as st)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[SaleText] = {
    withSQL {
      select.from(SaleText as st).where.append(where)
    }.map(SaleText(st.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[SaleText] = {
    withSQL {
      select.from(SaleText as st).where.append(where)
    }.map(SaleText(st.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(SaleText as st).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    title: String,
    text: Option[String]=None,
    languageid: Long,
    saleid: Long)(implicit session: DBSession = autoSession): SaleText = {
    withSQL {
      insert.into(SaleText).columns(
        column.title,
        column.text,
        column.languageid,
        column.saleid
      ).values(
        title,
        text,
        languageid,
        saleid
      )
    }.update.apply()

    SaleText(
      title = title,
      text = text,
      languageid = languageid,
      saleid = saleid)
  }

  def save(entity: SaleText)(implicit session: DBSession = autoSession): SaleText = {
    withSQL {
      update(SaleText).set(
        column.title -> entity.title,
        column.text -> entity.text,
        column.languageid -> entity.languageid,
        column.saleid -> entity.saleid
      ).where.eq(column.languageid, entity.languageid).and.eq(column.saleid, entity.saleid)
    }.update.apply()
    entity
  }

  def destroy(entity: SaleText)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(SaleText).where.eq(column.languageid, entity.languageid).and.eq(column.saleid, entity.saleid) }.update.apply()
  }

}
