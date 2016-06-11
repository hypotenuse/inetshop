package models

import scalikejdbc._
import validators.{JCostScaleUpdate, JCostScaleNew}

import scala.util.{Failure, Try}

case class CostScale(
  id: Long,
  start: Int,
  finish: Int,
  percent: BigDecimal) {

  def save()(implicit session: DBSession = CostScale.autoSession): CostScale = CostScale.save(this)(session)

  def destroy()(implicit session: DBSession = CostScale.autoSession): Unit = CostScale.destroy(this)(session)

}


object CostScale extends SQLSyntaxSupport[CostScale] {

  override val tableName = "costscales"

  override val columns = Seq("id", "start", "finish", "percent")

  def apply(cs: SyntaxProvider[CostScale])(rs: WrappedResultSet): CostScale = apply(cs.resultName)(rs)
  def apply(cs: ResultName[CostScale])(rs: WrappedResultSet): CostScale = new CostScale(
    id = rs.get(cs.id),
    start = rs.get(cs.start),
    finish = rs.get(cs.finish),
    percent = rs.get(cs.percent)
  )

  val cs = CostScale.syntax("cs")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[CostScale] = {
    withSQL {
      select.from(CostScale as cs).where.eq(cs.id, id)
    }.map(CostScale(cs.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[CostScale] = {
    withSQL(select.from(CostScale as cs)).map(CostScale(cs.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(CostScale as cs)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[CostScale] = {
    withSQL {
      select.from(CostScale as cs).where.append(where)
    }.map(CostScale(cs.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[CostScale] = {
    withSQL {
      select.from(CostScale as cs).where.append(where)
    }.map(CostScale(cs.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(CostScale as cs).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    start: Int,
    finish: Int,
    percent: BigDecimal)(implicit session: DBSession = autoSession): CostScale = {
    val generatedKey = withSQL {
      insert.into(CostScale).columns(
        column.start,
        column.finish,
        column.percent
      ).values(
        start,
        finish,
        percent
      )
    }.updateAndReturnGeneratedKey.apply()

    CostScale(
      id = generatedKey,
      start = start,
      finish = finish,
      percent = percent)
  }

  def create(validator: JCostScaleNew): Option[CostScale] = {
    if(clutter(validator.start, validator.finish)) None
    else {
      Some(create(
      start = validator.start,
      finish = validator.finish,
      percent = validator.percent
      ))
    }
  }

  private def clutter(start: Int, finish: Int, id: Option[Long] = None): Boolean = {
    val allScales = findAll()
    val scales: List[CostScale] = id.map{i =>
      allScales.filterNot(s=> s.id == i)
    }.getOrElse(allScales)
    val allSeq: List[Seq[Int]]= scales.map{s=>
      s.start to s.finish
    }
    val allInts: List[Int] = allSeq.flatten
    allInts.contains(start) || allInts.contains(finish)
  }

  def save(entity: CostScale)(implicit session: DBSession = autoSession): CostScale = {
    withSQL {
      update(CostScale).set(
        column.id -> entity.id,
        column.start -> entity.start,
        column.finish -> entity.finish,
        column.percent -> entity.percent
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(id:Long, validator: JCostScaleUpdate): Try[CostScale] = {
    find(id).map(s =>
      if(clutter(validator.start, validator.finish, Some(id))) Failure(new IllegalArgumentException)
      else {
        Try(s.copy(
          start = validator.start,
          finish = validator.finish,
          percent = validator.percent
        ).save())
      }
    ).getOrElse(Failure(new NoSuchElementException))

  }

  def destroy(entity: CostScale)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(CostScale).where.eq(column.id, entity.id) }.update.apply()
  }

}
