package models

import scalikejdbc._
import validators.{JSettingUpdate, JSettingNew}

import scala.util.{Success, Failure, Try}

case class Setting(
                    id: Long,
                    title: String,
                    value: String,
                    shortcode: String) {

  def save()(implicit session: DBSession = Setting.autoSession): Setting = Setting.save(this)(session)

  def destroy()(implicit session: DBSession = Setting.autoSession): Unit = Setting.destroy(this)(session)

}


object Setting extends SQLSyntaxSupport[Setting] {

  override val tableName = "settings"

  override val columns = Seq("id", "title", "value", "shortcode")

  def apply(s: SyntaxProvider[Setting])(rs: WrappedResultSet): Setting = apply(s.resultName)(rs)

  def apply(s: ResultName[Setting])(rs: WrappedResultSet): Setting = new Setting(
    id = rs.get(s.id),
    title = rs.get(s.title),
    value = rs.get(s.value),
    shortcode = rs.get(s.shortcode)
  )

  val s = Setting.syntax("s")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Setting] = {
    withSQL {
      select.from(Setting as s).where.eq(s.id, id)
    }.map(Setting(s.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Setting] = {
    withSQL(select.from(Setting as s)).map(Setting(s.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Setting as s)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Setting] = {
    withSQL {
      select.from(Setting as s).where.append(where)
    }.map(Setting(s.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Setting] = {
    withSQL {
      select.from(Setting as s).where.append(where)
    }.map(Setting(s.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Setting as s).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              title: String,
              value: String,
              shortcode: String)(implicit session: DBSession = autoSession): Setting = {
    val generatedKey = withSQL {
      insert.into(Setting).columns(
        column.title,
        column.value,
        column.shortcode
      ).values(
        title,
        value,
        shortcode
      )
    }.updateAndReturnGeneratedKey.apply()

    Setting(
      id = generatedKey,
      title = title,
      value = value,
      shortcode = shortcode)
  }

  def create(validator: JSettingNew): Try[Setting] = {
    val shortcodeDuplicated: Option[Setting] = Setting.findBy(sqls"shortcode = ${validator.shortcode}")

    shortcodeDuplicated.map{d =>
      Failure(new Exception("Shortcode is not unique"))
    }.getOrElse{
      Try(create(
        title = validator.title,
        value = validator.value,
        shortcode = validator.shortcode
      ))
    }

  }

  def save(entity: Setting)(implicit session: DBSession = autoSession): Setting = {
    withSQL {
      update(Setting).set(
        column.id -> entity.id,
        column.title -> entity.title,
        column.value -> entity.value,
        column.shortcode -> entity.shortcode
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(id: Long, validator: JSettingUpdate): Try[Setting] = {
    val shortcodeDuplicated: Option[Setting] = validator.shortcode.flatMap{code =>
      Setting.findBy(sqls"shortcode = ${code} and id <> ${id}")
    }

    shortcodeDuplicated.map{d =>
      Failure(new Exception("Shortcode is not unique"))
    }.getOrElse{
      find(id).map{s =>

        Try(s.copy(
          title = validator.title.getOrElse(s.title),
          value = validator.value.getOrElse(s.value),
          shortcode = validator.shortcode.getOrElse(s.shortcode)
        ).save())

      }.getOrElse(Failure(new IllegalArgumentException("Not found")))
    }

  }

  def destroy(entity: Setting)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(Setting).where.eq(column.id, entity.id)
    }.update.apply()
  }

}
