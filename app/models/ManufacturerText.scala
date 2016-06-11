package models

import scalikejdbc._

case class ManufacturerText(
  manufacturerid: Long,
  languageid: Long,
  title: String,
  description: Option[String] = None) {

  def save()(implicit session: DBSession = ManufacturerText.autoSession): ManufacturerText = ManufacturerText.save(this)(session)

  def destroy()(implicit session: DBSession = ManufacturerText.autoSession): Unit = ManufacturerText.destroy(this)(session)

}


object ManufacturerText extends SQLSyntaxSupport[ManufacturerText] {

  override val tableName = "manufacturer_texts"

  override val columns = Seq("manufacturerid", "languageid", "title", "description")

  def apply(mt: SyntaxProvider[ManufacturerText])(rs: WrappedResultSet): ManufacturerText = apply(mt.resultName)(rs)
  def apply(mt: ResultName[ManufacturerText])(rs: WrappedResultSet): ManufacturerText = new ManufacturerText(
    manufacturerid = rs.get(mt.manufacturerid),
    languageid = rs.get(mt.languageid),
    title = rs.get(mt.title),
    description = rs.get(mt.description)
  )

  val mt = ManufacturerText.syntax("mt")

  override val autoSession = AutoSession

  def find(languageid: Long, manufacturerid: Long)(implicit session: DBSession = autoSession): Option[ManufacturerText] = {
    withSQL {
      select.from(ManufacturerText as mt).where.eq(mt.languageid, languageid).and.eq(mt.manufacturerid, manufacturerid)
    }.map(ManufacturerText(mt.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ManufacturerText] = {
    withSQL(select.from(ManufacturerText as mt)).map(ManufacturerText(mt.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(ManufacturerText as mt)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ManufacturerText] = {
    withSQL {
      select.from(ManufacturerText as mt).where.append(where)
    }.map(ManufacturerText(mt.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ManufacturerText] = {
    withSQL {
      select.from(ManufacturerText as mt).where.append(where)
    }.map(ManufacturerText(mt.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(ManufacturerText as mt).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    manufacturerid: Long,
    languageid: Long,
    title: String,
    description: Option[String] = None)(implicit session: DBSession = autoSession): ManufacturerText = {
    withSQL {
      insert.into(ManufacturerText).columns(
        column.manufacturerid,
        column.languageid,
        column.title,
        column.description
      ).values(
        manufacturerid,
        languageid,
        title,
        description
      )
    }.update.apply()

    ManufacturerText(
      manufacturerid = manufacturerid,
      languageid = languageid,
      title = title,
      description = description)
  }

  def save(entity: ManufacturerText)(implicit session: DBSession = autoSession): ManufacturerText = {
    withSQL {
      update(ManufacturerText).set(
        column.manufacturerid -> entity.manufacturerid,
        column.languageid -> entity.languageid,
        column.title -> entity.title,
        column.description -> entity.description
      ).where.eq(column.languageid, entity.languageid).and.eq(column.manufacturerid, entity.manufacturerid)
    }.update.apply()
    entity
  }

  def destroy(entity: ManufacturerText)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(ManufacturerText).where.eq(column.languageid, entity.languageid).and.eq(column.manufacturerid, entity.manufacturerid) }.update.apply()
  }

}
