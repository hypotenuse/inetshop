package models

import scalikejdbc._
import validators.{JCurrencyUpdate, JCurrencyNew}

case class Currency(
                     id: Long,
                     title: String,
                     main: Boolean,
                     cros: BigDecimal) {

  def save()(implicit session: DBSession = Currency.autoSession): Currency = Currency.save(this)(session)

  def destroy()(implicit session: DBSession = Currency.autoSession): Unit = Currency.destroy(this)(session)

}


object Currency extends SQLSyntaxSupport[Currency] {

  override val tableName = "currencies"

  override val columns = Seq("id", "title", "main", "cros")

  def apply(c: SyntaxProvider[Currency])(rs: WrappedResultSet): Currency = apply(c.resultName)(rs)

  def apply(c: ResultName[Currency])(rs: WrappedResultSet): Currency = new Currency(
    id = rs.get(c.id),
    title = rs.get(c.title),
    main = rs.get(c.main),
    cros = rs.get(c.cros)
  )

  val c = Currency.syntax("c")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Currency] = {
    withSQL {
      select.from(Currency as c).where.eq(c.id, id)
    }.map(Currency(c.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Currency] = {
    withSQL(select.from(Currency as c)).map(Currency(c.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Currency as c)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Currency] = {
    withSQL {
      select.from(Currency as c).where.append(where)
    }.map(Currency(c.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Currency] = {
    withSQL {
      select.from(Currency as c).where.append(where)
    }.map(Currency(c.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Currency as c).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              title: String,
              main: Boolean,
              cros: BigDecimal)(implicit session: DBSession = autoSession): Currency = {
    val generatedKey = withSQL {
      insert.into(Currency).columns(
        column.title,
        column.main,
        column.cros
      ).values(
        title,
        main,
        cros
      )
    }.updateAndReturnGeneratedKey.apply()

    Currency(
      id = generatedKey,
      title = title,
      main = main,
      cros = cros)
  }


  def create(validator: JCurrencyNew): Option[Currency] = {
    if (validator.main && Currency.findAllBy(sqls"main = true").nonEmpty) None
    else
      Some(create(validator.title, validator.main, validator.cros))
  }

  def save(entity: Currency)(implicit session: DBSession = autoSession): Currency = {
    withSQL {
      update(Currency).set(
        column.id -> entity.id,
        column.title -> entity.title,
        column.main -> entity.main,
        column.cros -> entity.cros
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(id: Long, valor: JCurrencyUpdate): Option[Currency] = {
    find(id).map { c =>
      val mainExist = for{
        main <- valor.main
        if main
        curr <- Currency.findBy(sqls"main = true and id<>${c.id}")
      } yield curr

      mainExist.map{m =>
        None
      }.getOrElse {
        Some(c.copy(
          title = valor.title.getOrElse(c.title),
          main = valor.main.getOrElse(c.main),
          cros = valor.cros.getOrElse(c.cros)
        ).save())
      }

    }.getOrElse(throw new NoSuchElementException)
  }

  def destroy(entity: Currency)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(Currency).where.eq(column.id, entity.id)
    }.update.apply()
  }

}
