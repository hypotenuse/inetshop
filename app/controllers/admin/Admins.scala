package controllers.admin

import models.Admin
import models.Admin.adminOldPassIncorrect
import play.api.libs.json._
import play.api.mvc.Action
import validators.{JAdminUpdate, JAdminNew}

class Admins extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        import jreaders.AdminReaders.adminNewReads
        val json = request.body
        json.validate[JAdminNew].fold(
          valid = { jAdminNew =>
            import scalikejdbc._
            Admin.findBy(sqls"email = ${jAdminNew.email}").map{a =>
              BadRequest(Json.toJson(JsString("DUPLICATED_EMAIL")))
            }.getOrElse{
              val admin=Admin.create(jAdminNew)
              Ok(Json.toJson(JsNumber(admin.id)))
            }
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
        import jreaders.AdminReaders.adminUpdateReads
        val json = request.body
        json.validate[JAdminUpdate].fold(
          valid = { jAdmin =>
            try {
              Admin.save(id, jAdmin)
              Ok(Json.toJson(JsString("Saved")))
            }
            catch {
              case e: NoSuchElementException => NotFound
              case e: adminOldPassIncorrect => BadRequest(Json.toJson("OLD_PASSWORD_IS_INCORRECT"))
              case e: IllegalArgumentException => BadRequest(Json.toJson("SERVER_ERROR"))
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
        import jwriters.AdminWriters._
        val result = Json.obj("data" -> Admin.findAll())
        Ok(Json.toJson(result))
      }
  )

  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        import jwriters.AdminWriters.adminWriters
        Admin.find(id).map{
          n => Ok(Json.toJson(n))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        Admin.find(id).map{
          adm =>
            adm.destroy()
            Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )


}
