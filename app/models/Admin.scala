package models

import scalikejdbc._
import org.joda.time.{DateTime}
import services.auth.Authenticator
import validators.{JAdminUpdate, JAdminNew}

import scala.util.{Try, Success}

case class Admin(
                  id: Long,
                  email: String,
                  name: String,
                  pass: String,
                  sessionid: Option[String] = None,
                  changepassrequest: Option[String] = None,
                  lastvisit: Option[DateTime] = None) extends Authenticatable {

  def save()(implicit session: DBSession = Admin.autoSession): Admin = Admin.save(this)(session)

  def destroy()(implicit session: DBSession = Admin.autoSession): Unit = Admin.destroy(this)(session)

}


object Admin extends SQLSyntaxSupport[Admin] {

  override val tableName = "admins"

  override val columns = Seq("id", "email", "name", "pass", "sessionid", "changepassrequest", "lastvisit")

  def apply(a: SyntaxProvider[Admin])(rs: WrappedResultSet): Admin = apply(a.resultName)(rs)

  def apply(a: ResultName[Admin])(rs: WrappedResultSet): Admin = new Admin(
    id = rs.get(a.id),
    email = rs.get(a.email),
    name = rs.get(a.name),
    pass = rs.get(a.pass),
    sessionid = rs.get(a.sessionid),
    changepassrequest = rs.get(a.changepassrequest),
    lastvisit = rs.get(a.lastvisit)
  )

  val a = Admin.syntax("a")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Admin] = {
    withSQL {
      select.from(Admin as a).where.eq(a.id, id)
    }.map(Admin(a.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Admin] = {
    withSQL(select.from(Admin as a)).map(Admin(a.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Admin as a)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Admin] = {
    withSQL {
      select.from(Admin as a).where.append(where)
    }.map(Admin(a.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Admin] = {
    withSQL {
      select.from(Admin as a).where.append(where)
    }.map(Admin(a.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Admin as a).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              email: String,
              name: String,
              pass: String,
              sessionid: Option[String] = None,
              changepassrequest: Option[String] = None,
              lastvisit: Option[DateTime] = None)(implicit session: DBSession = autoSession): Admin = {
    val generatedKey = withSQL {
      insert.into(Admin).columns(
        column.email,
        column.name,
        column.pass,
        column.sessionid,
        column.changepassrequest,
        column.lastvisit
      ).values(
        email,
        name,
        pass,
        sessionid,
        changepassrequest,
        lastvisit
      )
    }.updateAndReturnGeneratedKey.apply()

    Admin(
      id = generatedKey,
      email = email,
      name = name,
      pass = pass,
      sessionid = sessionid,
      changepassrequest = changepassrequest,
      lastvisit = lastvisit)
  }

  def create(validator: JAdminNew): Admin = {
    create(
      email = validator.email,
      name = validator.name,
      pass = validator.pass
    )
  }

  def save(entity: Admin)(implicit session: DBSession = autoSession): Admin = {
    withSQL {
      update(Admin).set(
        column.id -> entity.id,
        column.email -> entity.email,
        column.name -> entity.name,
        column.pass -> entity.pass,
        column.sessionid -> entity.sessionid,
        column.changepassrequest -> entity.changepassrequest,
        column.lastvisit -> entity.lastvisit
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def save(id: Long, validator: JAdminUpdate): Admin = {
    val authenticator = new Authenticator
    val adm = find(id).getOrElse(throw new NoSuchElementException("Admin does not exist"))
    val tempHash: Option[Try[String]] = validator.pass.flatMap{newpass =>
      val oldpass = validator.oldpass.getOrElse(throw new adminOldPassIncorrect)
      authenticator.check(oldpass,adm.pass) match {
        case Success(r) if r == true => Some(authenticator.getSaltedHash(newpass))
        case _ => throw new adminOldPassIncorrect
      }
    }

    val newHash = tempHash.map{
      case Success(v) => v
      case _ => throw new IllegalArgumentException
    }

      adm.copy(
      email = validator.email.getOrElse(adm.email),
      pass = newHash.getOrElse(adm.pass)
      ).save()
  }

  def destroy(entity: Admin)(implicit session: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(Admin).where.eq(column.id, entity.id)
    }.update.apply()
  }

  class adminDuplicateEmail extends IllegalArgumentException
  class adminOldPassIncorrect extends IllegalArgumentException

}
