package services

import scalikejdbc._

import scala.util.{Failure, Success, Try}

object Repository {
  implicit val session = AutoSession
  def count(table: String, inputWhere: String): Try[Option[Int]]={
    val tableName = SQLSyntax.createUnsafely(table)
    val where = SQLSyntax.createUnsafely(inputWhere)
    try{
      Success {
        DB readOnly { implicit session =>
          sql"select COUNT(*) from ${tableName} WHERE ${where}"
            .map(rs => rs.int("count")).single.apply()
        }
      }
    }
    catch {
      case t: Throwable => Failure(t)
    }
  }

  def contains(table: String, inputWhere: String): Try[Boolean]={
    count(table, inputWhere) match {
      case Success(v) if v.get>0 => Success(true)
      case Success(v) if v.get==0 => Success(false)
      case Failure(t) => Failure(t)
    }
  }

  def getData(table: String, what: String, inputWhere: String, order: String, limit: Int = 10, offset: Int = 0)={
    val tableName = SQLSyntax.createUnsafely(table)
    val where = SQLSyntax.createUnsafely(inputWhere)
    val orderd = SQLSyntax.createUnsafely(order)
    val whatSelect = SQLSyntax.createUnsafely(what)
    DB readOnly { implicit session =>
      sql"select ${whatSelect} from ${tableName} WHERE ${where} ORDER BY ${orderd} LIMIT ${limit} OFFSET ${offset}"
    }
  }

}
