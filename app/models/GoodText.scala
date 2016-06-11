package models

import scalikejdbc._
import validators.JGoodNew

case class GoodText(
  goodid: Long,
  languageid: Long,
  title: String,
  description: Option[String] = None,
  descriptionShort: Option[String] = None,
  metatitle: Option[String] = None,
  metadescription: Option[String] = None) {

  def save()(implicit session: DBSession = GoodText.autoSession): GoodText = GoodText.save(this)(session)

  def destroy()(implicit session: DBSession = GoodText.autoSession): Unit = GoodText.destroy(this)(session)

}


object GoodText extends SQLSyntaxSupport[GoodText] {

  override val tableName = "good_texts"

  override val columns = Seq("goodid", "languageid", "title", "description", "description_short", "metatitle", "metadescription")

  def apply(gt: SyntaxProvider[GoodText])(rs: WrappedResultSet): GoodText = apply(gt.resultName)(rs)
  def apply(gt: ResultName[GoodText])(rs: WrappedResultSet): GoodText = new GoodText(
    goodid = rs.get(gt.goodid),
    title = rs.get(gt.title),
    description = rs.get(gt.description),
    descriptionShort = rs.get(gt.descriptionShort),
    metatitle = rs.get(gt.metatitle),
    metadescription = rs.get(gt.metadescription),
    languageid = rs.get(gt.languageid)
  )

  val gt = GoodText.syntax("gt")

  override val autoSession = AutoSession

  def find(goodid: Long, languageid: Long)(implicit session: DBSession = autoSession): Option[GoodText] = {
    withSQL {
      select.from(GoodText as gt).where.eq(gt.goodid, goodid).and.eq(gt.languageid, languageid)
    }.map(GoodText(gt.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[GoodText] = {
    withSQL(select.from(GoodText as gt)).map(GoodText(gt.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(GoodText as gt)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[GoodText] = {
    withSQL {
      select.from(GoodText as gt).where.append(where)
    }.map(GoodText(gt.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[GoodText] = {
    withSQL {
      select.from(GoodText as gt).where.append(where)
    }.map(GoodText(gt.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(GoodText as gt).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    goodid: Long,
    languageid: Long,
    title: String,
    description: Option[String] = None,
    descriptionShort: Option[String] = None,
    metatitle: Option[String] = None,
    metadescription: Option[String] = None
    )(implicit session: DBSession = autoSession): GoodText = {
    withSQL {
      insert.into(GoodText).columns(
        column.goodid,
        column.languageid,
        column.title,
        column.description,
        column.descriptionShort,
        column.metatitle,
        column.metadescription
      ).values(
        goodid,
        languageid,
        title,
        description,
        descriptionShort,
        metatitle,
        metadescription
      )
    }.update.apply()

    GoodText(
      goodid = goodid,
      languageid = languageid,
      title = title,
      description = description,
      descriptionShort = descriptionShort,
      metatitle = metatitle,
      metadescription = metadescription
      )
  }

  def create(goodid: Long, languageId: Long, title: String): GoodText ={
    create(
      goodid,
      languageId,
      title,
      None,
      None,
      None,
      None
    )
  }


  def save(entity: GoodText)(implicit session: DBSession = autoSession): GoodText = {
    withSQL {
      update(GoodText).set(
        column.goodid -> entity.goodid,
        column.title -> entity.title,
        column.description -> entity.description,
        column.descriptionShort -> entity.descriptionShort,
        column.metatitle -> entity.metatitle,
        column.metadescription -> entity.metadescription,
        column.languageid -> entity.languageid
      ).where.eq(column.goodid, entity.goodid).and.eq(column.languageid, entity.languageid)
    }.update.apply()
    entity
  }

  def destroy(entity: GoodText)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(GoodText).where.eq(column.goodid, entity.goodid).and.eq(column.languageid, entity.languageid) }.update.apply()
  }

}
