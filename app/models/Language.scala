package models

import scalikejdbc._

case class Language(
  id: Long,
  cod: String,
  enabled: Option[Boolean] = None,
  defaultlng: Option[Boolean] = None) {

  def save()(implicit session: DBSession = Language.autoSession): Language = Language.save(this)(session)

  def destroy()(implicit session: DBSession = Language.autoSession): Unit = Language.destroy(this)(session)

}


object Language extends SQLSyntaxSupport[Language] {

  override val tableName = "languages"

  override val columns = Seq("id", "cod", "enabled", "defaultlng")

  def apply(l: SyntaxProvider[Language])(rs: WrappedResultSet): Language = apply(l.resultName)(rs)
  def apply(l: ResultName[Language])(rs: WrappedResultSet): Language = new Language(
    id = rs.get(l.id),
    cod = rs.get(l.cod),
    enabled = rs.get(l.enabled),
    defaultlng = rs.get(l.defaultlng)
  )

  val l = Language.syntax("l")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Language] = {
    withSQL {
      select.from(Language as l).where.eq(l.id, id)
    }.map(Language(l.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Language] = {
    withSQL(select.from(Language as l)).map(Language(l.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Language as l)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Language] = {
    withSQL {
      select.from(Language as l).where.append(where)
    }.map(Language(l.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Language] = {
    withSQL {
      select.from(Language as l).where.append(where)
    }.map(Language(l.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Language as l).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    cod: String,
    enabled: Option[Boolean] = None,
    defaultlng: Option[Boolean] = None)(implicit session: DBSession = autoSession): Language = {
    val generatedKey = withSQL {
      insert.into(Language).columns(
        column.cod,
        column.enabled,
        column.defaultlng
      ).values(
        cod,
        enabled,
        defaultlng
      )
    }.updateAndReturnGeneratedKey.apply()

    Language(
      id = generatedKey,
      cod = cod,
      enabled = enabled,
      defaultlng = defaultlng)
  }

  def save(entity: Language)(implicit session: DBSession = autoSession): Language = {
    withSQL {
      update(Language).set(
        column.id -> entity.id,
        column.cod -> entity.cod,
        column.enabled -> entity.enabled,
        column.defaultlng -> entity.defaultlng
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Language)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Language).where.eq(column.id, entity.id) }.update.apply()
  }

  def getByCod(cod: String): Option[Language] ={
    Language.findBy(sqls"cod = ${cod}")
  }

  def getDefault: Option[Language] = {
    Language.findBy(sqls"defaultlng = TRUE")
  }

}
