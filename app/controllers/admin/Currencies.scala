package controllers.admin

import java.util.NoSuchElementException

import jreaders.CurrencyReaders._
import jwriters.CurrencyWriters._
import models.Currency
import play.api.libs.json._
import play.api.mvc.Action
import validators.{JCurrencyNew, JCurrencyUpdate}

class Currencies extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JCurrencyNew].fold(
          valid = { jCurrency =>
            val currency=Currency.create(jCurrency)
            currency.map{c =>
              Ok(Json.toJson(JsNumber(c.id)))
            }.getOrElse(BadRequest(Json.toJson(JsString("Main currency already exist"))))

          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )
      }
  )

  def update(id: Long) = admin(
    admin =>
      Action(parse.json) {implicit request =>
        val json = request.body
        json.validate[JCurrencyUpdate].fold(
          valid = { jCurrencyUpdate =>
            try {
              val cur = Currency.save(id, jCurrencyUpdate)
              cur.map(c=> Ok(Json.toJson(JsString("Saved")))).getOrElse(
                BadRequest(Json.toJson(JsString("Main currency already exist")))
              )
            }
            catch {
              case e: NoSuchElementException => NotFound
            }
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )

      }
  )

  def list = admin(
    admin =>
      Action { implicit request =>
        val result = Json.obj("data" -> Currency.findAll())
        Ok(Json.toJson(result))
      }
  )


  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        Currency.find(id).map{
          n => Ok(Json.toJson(n))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        Currency.find(id).map{
          currency =>
              currency.destroy()
              Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )


}
