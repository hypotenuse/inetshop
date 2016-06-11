package models

import scalikejdbc._
import services.Repository
import validators.{JSaleUpdate, JSaleNew}

import scala.util.{Failure, Success}

case class Sale(
  id: Long,
  titlecolorbackgrnd: String) {

  def save()(implicit session: DBSession = Sale.autoSession): Sale = Sale.save(this)(session)

  def destroy()(implicit session: DBSession = Sale.autoSession): Unit = Sale.destroy(this)(session)

  def textByDefaultLang(implicit session: DBSession = Sale.autoSession): Option[SaleText] = Sale.getTextByDefaultLang(this)(session)

  def addGood(good: Good)(implicit session: DBSession = Sale.autoSession): Boolean = Sale.addGood(this, good)(session)

  def removeGood(good: Good)(implicit session: DBSession = Sale.autoSession): Boolean = Sale.removeGood(this, good)(session)

  def removeGoods()(implicit session: DBSession = Sale.autoSession): Boolean = Sale.removeGoods(this)(session)

  def goods()(implicit session: DBSession = Sale.autoSession): List[Good] = Sale.goods(this)(session)
}


object Sale extends SQLSyntaxSupport[Sale] {

  override val tableName = "sales"

  override val columns = Seq("id", "titlecolorbackgrnd")

  def apply(s: SyntaxProvider[Sale])(rs: WrappedResultSet): Sale = apply(s.resultName)(rs)
  def apply(s: ResultName[Sale])(rs: WrappedResultSet): Sale = new Sale(
    id = rs.get(s.id),
    titlecolorbackgrnd = rs.get(s.titlecolorbackgrnd)
  )

  val s = Sale.syntax("s")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Sale] = {
    withSQL {
      select.from(Sale as s).where.eq(s.id, id)
    }.map(Sale(s.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Sale] = {
    withSQL(select.from(Sale as s)).map(Sale(s.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Sale as s)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Sale] = {
    withSQL {
      select.from(Sale as s).where.append(where)
    }.map(Sale(s.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Sale] = {
    withSQL {
      select.from(Sale as s).where.append(where)
    }.map(Sale(s.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Sale as s).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    titlecolorbackgrnd: String)(implicit session: DBSession = autoSession): Sale = {
    val generatedKey = withSQL {
      insert.into(Sale).columns(
        column.titlecolorbackgrnd
      ).values(
        titlecolorbackgrnd
      )
    }.updateAndReturnGeneratedKey.apply()

    Sale(
      id = generatedKey,
      titlecolorbackgrnd = titlecolorbackgrnd)
  }

  def create(jSale: JSaleNew): Sale ={
    val sale = create(
      titlecolorbackgrnd = jSale.titlecolorbackgrnd
    )
    Language.findBy(sqls"defaultlng = true").map{
      l =>
        SaleText.create(jSale.title, None, l.id, sale.id)
    }.getOrElse{
      throw new Exception("Can't find default language in database!")
    }

    sale
  }

  def save(entity: Sale)(implicit session: DBSession = autoSession): Sale = {
    withSQL {
      update(Sale).set(
        column.id -> entity.id,
        column.titlecolorbackgrnd -> entity.titlecolorbackgrnd
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(newsId: Long, jSale: JSaleUpdate): Sale ={
    val maybeSale = Sale.find(newsId)
    val lang = Language.getByCod(jSale.languagecod).getOrElse{
      throw new Exception("Can't find language with code ${jSale.languagecod} in database!")}

    maybeSale.map{
      sale =>
        SaleText.find(lang.id, sale.id).map{
          t =>
            t.copy(
              title = jSale.title.getOrElse(t.title),
              text = jSale.text.orElse(t.text)
            ).save()

        }.getOrElse{
          jSale.title.map{
            t =>
              SaleText.create(
                saleid = sale.id,
                languageid = lang.id,
                title = t,
                text = jSale.text
              )
          }.getOrElse(throw new IllegalArgumentException(s"Can't create text for language ${lang.cod} with empty title"))

        }
        sale.copy(titlecolorbackgrnd = jSale.titlecolorbackgrnd.getOrElse(sale.titlecolorbackgrnd)).save()

    }.getOrElse(throw new NoSuchElementException("Sale does not exist"))

  }

  def destroy(entity: Sale)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Sale).where.eq(column.id, entity.id) }.update.apply()
  }

  def getTextByDefaultLang(entity: Sale)(implicit session: DBSession = autoSession): Option[SaleText]={
    for{
      lan <- Language.findBy(sqls"defaultlng = TRUE")
      text <- SaleText.find(lan.id, entity.id)
    } yield text
  }

  def addGood(sale: Sale, good: Good)(implicit session: DBSession = Sale.autoSession): Boolean={
    def insert(): Boolean ={
      sql"""insert into sale_good(saleid, goodid)
               values (${sale.id}, ${good.id})"""
        .update().apply()
      true
    }
    Repository.contains("sale_good", s"goodid=${good.id} AND saleid=${sale.id}") match {
      case Success(x) if !x => insert()
      case Failure(t) => throw t
      case _ => true
    }
  }

  def removeGood(sale: Sale, good: Good)(implicit session: DBSession = Sale.autoSession): Boolean={
    implicit val session = AutoSession
    def remove(): Boolean ={
      sql"""DELETE FROM sale_good WHERE goodid = ${good.id} AND saleid = ${sale.id}"""
        .update.apply()
      true
    }
    Repository.contains("sale_good", s"goodid=${good.id} AND saleid=${sale.id}") match {
      case Success(x) if x => remove()
      case Failure(t) => throw t
      case _ => true
    }

  }

  def removeGoods(sale: Sale)(implicit session: DBSession = Sale.autoSession): Boolean={
    implicit val session = AutoSession
      sql"""DELETE FROM sale_good WHERE saleid = ${sale.id}"""
        .update.apply()
      true
  }

  def goods(sale: Sale)(implicit session: DBSession = Category.autoSession): List[Good] = {
    DB readOnly { implicit session =>
        sql"select * from sale_good JOIN goods on goods.id=sale_good.goodid WHERE saleid=${sale.id}"
          .map(rs => Good(rs)).list().apply()
    }
  }

}
