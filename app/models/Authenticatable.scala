package models

/**
 * Can be authenticated
 */
trait Authenticatable {
  val email: String
  val pass: String
  val id: Long
  val sessionid: Option[String]
}
