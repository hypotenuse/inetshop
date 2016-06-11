package models

import scalikejdbc._
import traits.{HaveThumb, HavePicture}

case class Slide(
  id: Long,
  data: Array[Byte],
  url: Option[String] = None,
  extension: String) extends HavePicture with HaveThumb{
  val pictureDir = "slides"
  val picture = Some(data)
  def save()(implicit session: DBSession = Slide.autoSession): Slide = Slide.save(this)(session)
  def save(data: Option[Array[Byte]], url: Option[String], extension: Option[String]): Slide = Slide.save(data = data, url = url, extension = extension, entity = this)
  def pictureUrl(baseUrl: String): String = Slide.pictureUrl(baseUrl, this)
  def thumbUrl(baseUrl: String): String = Slide.thumbUrl(baseUrl, this)
  def destroy()(implicit session: DBSession = Slide.autoSession): Unit = Slide.destroy(this)(session)
  def pictureExtension : String = Slide.pictureExtension(this)
}


object Slide extends SQLSyntaxSupport[Slide] {

  override val tableName = "slides"

  override val columns = Seq("id", "data", "url", "extension")

  def apply(s: SyntaxProvider[Slide])(rs: WrappedResultSet): Slide = apply(s.resultName)(rs)
  def apply(s: ResultName[Slide])(rs: WrappedResultSet): Slide = new Slide(
    id = rs.get(s.id),
    data = rs.get(s.data),
    url = rs.get(s.url),
    extension = rs.get(s.extension)
  )

  val s = Slide.syntax("s")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Slide] = {
    withSQL {
      select.from(Slide as s).where.eq(s.id, id)
    }.map(Slide(s.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Slide] = {
    withSQL(select.from(Slide as s)).map(Slide(s.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Slide as s)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Slide] = {
    withSQL {
      select.from(Slide as s).where.append(where)
    }.map(Slide(s.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Slide] = {
    withSQL {
      select.from(Slide as s).where.append(where)
    }.map(Slide(s.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Slide as s).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    data: Array[Byte],
    url: Option[String] = None,
    extension: String)(implicit session: DBSession = autoSession): Slide = {
    val generatedKey = withSQL {
      insert.into(Slide).columns(
        column.data,
        column.url,
        column.extension
      ).values(
        data,
        url,
        extension
      )
    }.updateAndReturnGeneratedKey.apply()

    Slide(
      id = generatedKey,
      data = data,
      url = url,
      extension = extension)
  }

  def save(entity: Slide)(implicit session: DBSession = autoSession): Slide = {
    withSQL {
      update(Slide).set(
        column.id -> entity.id,
        column.data -> entity.data,
        column.url -> entity.url,
        column.extension -> entity.extension
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(data: Option[Array[Byte]], url: Option[String], extension: Option[String], entity: Slide): Slide = {
    implicit val session: DBSession = Slide.autoSession
    withSQL {
      update(Slide).set(
        column.id -> entity.id,
        column.data -> data.getOrElse(entity.data),
        column.url -> url.getOrElse(""),
        column.extension -> extension.getOrElse(entity.extension)
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Slide)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Slide).where.eq(column.id, entity.id) }.update.apply()
    entity.deletePicture()
    entity.deleteThumb()
  }

  def pictureUrl(baseUrl: String, entity: Slide): String = {
    baseUrl + entity.pictureDir + "/pictures/" + entity.id + "." + entity.extension
  }

  def thumbUrl(baseUrl: String, entity: Slide): String = {
    baseUrl + "thumbs/" + entity.pictureDir + "/" + entity.id + ".jpg"
  }

  def pictureExtension(slide: Slide): String ={
    slide.extension
  }
}
