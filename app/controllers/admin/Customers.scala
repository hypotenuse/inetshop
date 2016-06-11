package controllers.admin

import java.util.NoSuchElementException


import models.Customer
import play.api.libs.json._
import play.api.mvc.Action

class Customers extends BaseSecuredController{


  def list = admin(
    admin =>
      Action { implicit request =>
        import jwriters.CustomerWriters._
        val result = Json.obj("data" -> Customer.findAll())
        Ok(Json.toJson(result))
      }
  )


}
