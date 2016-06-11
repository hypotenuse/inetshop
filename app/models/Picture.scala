package models

import scalikejdbc._
import traits.HaveThumb

case class Picture(
  id: Long,
  extension: String,
  data: Array[Byte]) extends HaveThumb{

  val pictureDir = "goods"
  def save()(implicit session: DBSession = Picture.autoSession): Picture = Picture.save(this)(session)

  def destroy()(implicit session: DBSession = Picture.autoSession): Unit = Picture.destroy(this)(session)

}


object Picture extends SQLSyntaxSupport[Picture] {

  override val tableName = "pictures"

  override val columns = Seq("id", "data", "extension")

  def apply(p: SyntaxProvider[Picture])(rs: WrappedResultSet): Picture = apply(p.resultName)(rs)
  def apply(p: ResultName[Picture])(rs: WrappedResultSet): Picture = new Picture(
    id = rs.get(p.id),
    data = rs.get(p.data),
    extension = rs.get(p.extension)
  )
  def apply(rs: WrappedResultSet) = new Picture(
    id = rs.get("id"),
    data = rs.get("data"),
    extension = rs.get("extension")
  )

  val p = Picture.syntax("p")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Picture] = {
    withSQL {
      select.from(Picture as p).where.eq(p.id, id)
    }.map(Picture(p.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Picture] = {
    withSQL(select.from(Picture as p)).map(Picture(p.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Picture as p)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Picture] = {
    withSQL {
      select.from(Picture as p).where.append(where)
    }.map(Picture(p.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Picture] = {
    withSQL {
      select.from(Picture as p).where.append(where)
    }.map(Picture(p.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Picture as p).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    data: Array[Byte], extension: String)(implicit session: DBSession = autoSession): Picture = {
    val generatedKey = withSQL {
      insert.into(Picture).columns(
        column.data,
        column.extension
      ).values(
        data,
        extension
      )
    }.updateAndReturnGeneratedKey.apply()

    Picture(
      id = generatedKey,
      data = data,
      extension = extension
    )
  }

  def save(entity: Picture)(implicit session: DBSession = autoSession): Picture = {
    withSQL {
      update(Picture).set(
        column.id -> entity.id,
        column.data -> entity.data,
        column.extension -> entity.extension
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Picture)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Picture).where.eq(column.id, entity.id) }.update.apply()
    entity.deleteThumb()
  }


}
