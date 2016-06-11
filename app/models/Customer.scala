package models

import scalikejdbc._
import org.joda.time.{DateTime}

case class Customer(
  id: Long,
  email: String,
  name: String,
  pass: String,
  phone: Option[String] = None,
  changepassrequest: Option[String] = None,
  spent: BigDecimal = 0,
  lastvisit: Option[DateTime] = None,
  sessionid: Option[String] = None) extends Authenticatable{

  def save()(implicit session: DBSession = Customer.autoSession): Customer = Customer.save(this)(session)

  def destroy()(implicit session: DBSession = Customer.autoSession): Unit = Customer.destroy(this)(session)

}


object Customer extends SQLSyntaxSupport[Customer] {

  override val tableName = "customers"

  override val columns = Seq("id", "email", "name", "pass", "phone", "changepassrequest", "spent", "lastvisit", "sessionid")

  def apply(c: SyntaxProvider[Customer])(rs: WrappedResultSet): Customer = apply(c.resultName)(rs)
  def apply(c: ResultName[Customer])(rs: WrappedResultSet): Customer = new Customer(
    id = rs.get(c.id),
    email = rs.get(c.email),
    name = rs.get(c.name),
    pass = rs.get(c.pass),
    phone = rs.get(c.phone),
    changepassrequest = rs.get(c.changepassrequest),
    spent = rs.get(c.spent),
    lastvisit = rs.get(c.lastvisit),
    sessionid = rs.get(c.sessionid)
  )

  val c = Customer.syntax("c")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Customer] = {
    withSQL {
      select.from(Customer as c).where.eq(c.id, id)
    }.map(Customer(c.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Customer] = {
    withSQL(select.from(Customer as c)).map(Customer(c.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Customer as c)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Customer] = {
    withSQL {
      select.from(Customer as c).where.append(where)
    }.map(Customer(c.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Customer] = {
    withSQL {
      select.from(Customer as c).where.append(where)
    }.map(Customer(c.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Customer as c).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    email: String,
    name: String,
    pass: String,
    phone: Option[String] = None,
    changepassrequest: Option[String] = None,
    spent: BigDecimal = 0,
    lastvisit: Option[DateTime] = None,
    sessionid: Option[String] = None)(implicit session: DBSession = autoSession): Customer = {
    val generatedKey = withSQL {
      insert.into(Customer).columns(
        column.email,
        column.name,
        column.pass,
        column.phone,
        column.changepassrequest,
        column.spent,
        column.lastvisit,
        column.sessionid
      ).values(
        email,
        name,
        pass,
        phone,
        changepassrequest,
        spent,
        lastvisit,
        sessionid
      )
    }.updateAndReturnGeneratedKey.apply()

    Customer(
      id = generatedKey,
      email = email,
      name = name,
      pass = pass,
      phone = phone,
      changepassrequest = changepassrequest,
      spent = spent,
      lastvisit = lastvisit,
      sessionid = sessionid)
  }

  def save(entity: Customer)(implicit session: DBSession = autoSession): Customer = {
    withSQL {
      update(Customer).set(
        column.id -> entity.id,
        column.email -> entity.email,
        column.name -> entity.name,
        column.pass -> entity.pass,
        column.phone -> entity.phone,
        column.changepassrequest -> entity.changepassrequest,
        column.spent -> entity.spent,
        column.lastvisit -> entity.lastvisit,
        column.sessionid -> entity.sessionid
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Customer)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(Customer).where.eq(column.id, entity.id) }.update.apply()
  }

}
