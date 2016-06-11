package controllers.admin

import models.ExcelSetting
import play.api.libs.json._
import play.api.mvc.Action
import validators.JExcelSetting

class ExcelSettings extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        import jreaders.ExcelSettingReaders.excelSettingReads
        val json = request.body
        json.validate[JExcelSetting].fold(
          valid = { jExcelSetting =>
              val excelSetting=ExcelSetting.create(jExcelSetting)
              Ok(Json.toJson(excelSetting.id))
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
        import jreaders.ExcelSettingReaders.excelSettingReads
        val json = request.body
        json.validate[JExcelSetting].fold(
          valid = { jExcelSetting =>
              ExcelSetting.save(id, jExcelSetting).map{
                s =>
                  Ok(Json.toJson(JsString("Saved")))
              }.getOrElse(NotFound)
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
        import jwriters.ExcelSettingWriters.ExcelSettingListWrites
        val result = Json.obj("data" -> ExcelSetting.findAll())
        Ok(Json.toJson(result))
      }
  )

  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        import jwriters.ExcelSettingWriters.excelSettingWrites
        ExcelSetting.find(id).map{s =>
          Ok(Json.toJson(s))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        ExcelSetting.find(id).map{
          excelSetting =>
            excelSetting.destroy()
            Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )




}
