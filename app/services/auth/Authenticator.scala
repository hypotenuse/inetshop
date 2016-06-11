package services.auth

import models.Authenticatable
import org.joda.time.DateTime
import play.Logger

import scala.util.{Success, Failure, Try}
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.SecureRandom
import org.apache.commons.codec.binary.Base64
import scalikejdbc._




class Authenticator {
  private val iterations: Int = 20 * 1000
  private val saltLen : Int = 32
  private val desiredKeyLen : Int = 256

  def getSaltedHash(password:String): Try[String] = {
    val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen)
    // store the salt with the password
    hash(password, salt).map(Base64.encodeBase64String(salt) + "$" + _)
  }

  /** Checks whether given plaintext password corresponds
        to a stored salted hash of the password. */
  def check(password: String, stored: String):Try[Boolean] ={
     val saltAndPass: Array[String] = stored.split("\\$")
    if (saltAndPass.length != 2) {
      Failure(new IllegalStateException(
        "The stored password should have the form 'salt$hash'"))
    }
    else {
      val hashOfInput = hash(password, Base64.decodeBase64(saltAndPass(0)))
      hashOfInput.map(_.equals(saltAndPass(1))) match {
        case Success(r) => Success(r)
        case Failure(e) => Failure(e)
      }
    }
  }

  // using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
  // cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
  private def hash(password : String , salt: Array[Byte] ):Try[String]= {
    if (password == null || password.length() == 0){
      Failure( new IllegalArgumentException("Empty passwords are not supported."))
    }
    else {
      val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
      val key : SecretKey= f.generateSecret(new PBEKeySpec(
        password.toCharArray, salt, iterations, desiredKeyLen)
      )
      Success(Base64.encodeBase64String(key.getEncoded))
    }
  }

  def adminAuth(email: String, password: String): Option[models.Admin] ={
    checkPass(models.Admin.findBy(sqls"email = $email"), password).map(_.copy(sessionid = Some(getSessionToken), lastvisit = Some(DateTime.now())).save())
  }

  def customerAuth(email: String, password: String): Option[models.Customer] ={
    checkPass(models.Customer.findBy(sqls"email = $email"), password).map(_.copy(sessionid = Some(getSessionToken), lastvisit = Some(DateTime.now())).save())
  }

  private def checkPass [A <: Authenticatable] (model: Option[A], password: String): Option[A]={
    model match {
      case Some(m) => check(password, m.pass) match {
        case Success(true) => Some(m)
        case _ => None
      }
      case _ => None
    }
  }

  private def getSessionToken: String = Base64.encodeBase64String(SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen))
}