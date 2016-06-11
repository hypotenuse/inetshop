package models

import play.api.Logger
import scalikejdbc._
import validators.{JManufacturerUpdate, JManufacturerNew}

case class Manufacturer(
  id: Long) {

  def save()(implicit session: DBSession = Manufacturer.autoSession): Manufacturer = Manufacturer.save(this)(session)

  def destroy()(implicit session: DBSession = Manufacturer.autoSession): Unit = Manufacturer.destroy(this)(session)

  def texts()(implicit session: DBSession = Manufacturer.autoSession): List[ManufacturerText] = Manufacturer.getTexts(this)(session)
  def textByLang(languageid: Long)(implicit session: DBSession = Manufacturer.autoSession): Option[ManufacturerText] = Manufacturer.getTextByLang(this, languageid)(session)
  def textByDefaultLang(implicit session: DBSession = Manufacturer.autoSession): Option[ManufacturerText] = Manufacturer.getTextByDefaultLang(this)(session)

}


object Manufacturer extends SQLSyntaxSupport[Manufacturer] {

  override val tableName = "manufacturers"

  override val columns = Seq("id")

  def apply(m: SyntaxProvider[Manufacturer])(rs: WrappedResultSet): Manufacturer = apply(m.resultName)(rs)
  def apply(m: ResultName[Manufacturer])(rs: WrappedResultSet): Manufacturer = new Manufacturer(
    id = rs.get(m.id)
  )

  val m = Manufacturer.syntax("m")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Manufacturer] = {
    withSQL {
      select.from(Manufacturer as m).where.eq(m.id, id)
    }.map(Manufacturer(m.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Manufacturer] = {
    withSQL(select.from(Manufacturer as m)).map(Manufacturer(m.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Manufacturer as m)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Manufacturer] = {
    withSQL {
      select.from(Manufacturer as m).where.append(where)
    }.map(Manufacturer(m.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Manufacturer] = {
    withSQL {
      select.from(Manufacturer as m).where.append(where)
    }.map(Manufacturer(m.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Manufacturer as m).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create()(implicit session: DBSession = autoSession): Manufacturer = {
    val generatedKey = sql"INSERT INTO manufacturers DEFAULT VALUES;".updateAndReturnGeneratedKey.apply()

    Manufacturer(
      id = generatedKey)
  }

  def create(jMan: JManufacturerNew): Manufacturer ={
    val manufacturer = create()
    Language.findBy(sqls"defaultlng = true").map{
      l =>
        ManufacturerText.create(manufacturer.id, l.id, jMan.title)
    }.getOrElse{
      throw new Exception("Can't find default language in database!")
    }

    manufacturer
  }

  def save(entity: Manufacturer)(implicit session: DBSession = autoSession): Manufacturer = {
    withSQL {
      update(Manufacturer).set(
        column.id -> entity.id
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(manufacturerId: Long, jMan: JManufacturerUpdate): Manufacturer ={
    val maybeManufacturer = Manufacturer.find(manufacturerId)
    val lang = Language.getByCod(jMan.languagecod).getOrElse{
      throw new Exception("Can't find language with code ${jMan.languagecod} in database!")}

    maybeManufacturer.map{
      manufacturer =>
        ManufacturerText.find(lang.id, manufacturer.id).map{
          t =>
            t.copy(
              t.manufacturerid,
              t.languageid,
              jMan.title.getOrElse(t.title),
              jMan.description.orElse(t.description)
            ).save()

        }.getOrElse{
          jMan.title.map{
            t =>
              ManufacturerText.create(
                manufacturerid = manufacturer.id,
                languageid = lang.id,
                title = t,
                description = jMan.description
              )
          }.getOrElse(throw new IllegalArgumentException(s"Can't create text for language ${lang.cod} with empty title"))

        }
        manufacturer.copy(
          id = manufacturer.id
        ).save()

    }.getOrElse(throw new NoSuchElementException("Manufacturer does not exist"))

  }

  def destroy(entity: Manufacturer)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Manufacturer).where.eq(column.id, entity.id) }.update.apply()
  }

  def getTexts(entity: Manufacturer)(implicit session: DBSession = autoSession): List[ManufacturerText]={
    ManufacturerText.findAllBy(sqls"manufacturerid = ${entity.id}")
  }

  def getTextByLang(entity: Manufacturer, languageid: Long)(implicit session: DBSession = autoSession): Option[ManufacturerText]={
    ManufacturerText.findBy(sqls"manufacturerid = ${entity.id} and languageid = ${languageid}")
  }

  def getTextByDefaultLang(entity: Manufacturer)(implicit session: DBSession = autoSession): Option[ManufacturerText]={
    for{
      lan <- Language.findBy(sqls"defaultlng = TRUE")
      text <- ManufacturerText.findBy(sqls"manufacturerid = ${entity.id} and languageid = ${lan.id}")
    } yield text
  }
}
