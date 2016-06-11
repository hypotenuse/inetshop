package controllers.admin

import java.util.NoSuchElementException

import jreaders.SettingReaders._
import jwriters.SettingWriters._
import models.Setting
import play.api.libs.json._
import play.api.mvc.Action
import services.FileService
import validators.{JSettingNew, JSettingUpdate}

class Settings extends BaseSecuredController {


  def add = admin(
    admin =>
      Action(parse.json) { implicit request =>
        val json = request.body
        json.validate[JSettingNew].fold(
          valid = { jSettingNew =>
            val setting = Setting.create(jSettingNew)
            setting.map { s =>
              Ok(Json.toJson(JsNumber(s.id)))
            }.recover { case e: Exception =>
              BadRequest(Json.toJson(JsString("SHORTCODE_NOT_UNIQUE")))
            }.get
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )
      }
  )

  def update(id: Long) = admin(
    admin =>
      Action(parse.json) { implicit request =>
        val json = request.body
        json.validate[JSettingUpdate].fold(
          valid = { jSetting =>
            Setting.save(id, jSetting).map { s =>
              Ok(Json.toJson(JsString("Saved")))
            }.recover{
              case e: IllegalArgumentException => NotFound
              case e: Exception => BadRequest(Json.toJson(JsString("SHORTCODE_NOT_UNIQUE")))
            }.get
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
        val result = Json.obj("data" -> Setting.findAll())
        Ok(Json.toJson(result))
      }
  )


  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        Setting.find(id).map {
          setting => Ok(Json.toJson(setting))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        Setting.find(id).map {
          setting =>
            setting.destroy()
            Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )

}
