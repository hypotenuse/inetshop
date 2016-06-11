package controllers.admin


import java.util.UUID
import formvalidators.PriceValidator
import messages.{Statuses, NewPrice}
import models.ExcelSetting
import org.apache.commons.io.FilenameUtils
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import play.api.mvc.Action
import javax.inject.{Named, Inject}
import akka.actor.{ActorSystem, ActorRef}
import play.api.Play.current
import play.api.i18n.{I18nSupport, MessagesApi}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.Await


class Prices @Inject() (val system: ActorSystem, @Named("excel-manager-actor") val managerActor: ActorRef, @Named("price-manager-actor") val priceManagerActor: ActorRef, val messagesApi: MessagesApi) extends BaseSecuredController  with I18nSupport{

  val uploadForm = Form(mapping(
    "settingId" -> longNumber)(PriceValidator.apply)(PriceValidator.unapply))

  def add = admin(
    admin =>
      Action(parse.multipartFormData) { implicit request =>
        uploadForm.bindFromRequest().fold(
          hasErrors => BadRequest(hasErrors.errorsAsJson),
          validator => {
            ExcelSetting.find(validator.settingId).map { setting =>
              request.body.file("price") match {
                case Some(file) =>
                  import java.io.File
                  file.contentType match {
                    case Some(
                    "application/vnd.ms-excel" |
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" |
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" |
                    "application/vnd.ms-excel" |
                    "application/msexcel" |
                    "application/x-msexcel" |
                    "application/x-ms-excel" |
                    "application/x-excel" |
                    "application/x-dos_ms_excel" |
                    "application/xls" |
                    "application/x-xls"
                    ) =>
                      current.configuration.getString("prices.path").map { dir =>
                        new File(current.path + dir).mkdirs()
                        val extension = FilenameUtils.getExtension(file.filename)
                        val filename = UUID.randomUUID() + "." + extension
                        val path =current.path + dir + filename
                        file.ref.moveTo(new File(path))
                        managerActor ! NewPrice(settingId = setting.id, supplierId = setting.supplier, filename = filename)
                        Ok(Json.toJson(JsString("Added")))
                      }.getOrElse(BadRequest(Json.toJson(JsString("Configuration_error"))))
                    case _ => BadRequest(Json.toJson(JsObject(Map("obj.price" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.unsupported.file")))))))))
                  }
                case _ => BadRequest(Json.toJson(JsObject(Map("obj.price" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.required")))))))))
              }
            }.getOrElse(BadRequest(Json.toJson(JsObject(Map("obj.settingId" -> JsArray(Seq(JsObject(Map("msg" -> JsString("error.notfound"))))))))))
          }
        )
      })

  def list() = admin(
    admin =>
      Action {
        implicit request =>
          import jwriters.StatusWriters.priceStatusesWriter
          implicit val timeout = Timeout(5.seconds)
          val future = managerActor ? "status"
          val result = Await.result(future, timeout.duration).asInstanceOf[Statuses]
          Ok(Json.toJson(result))
      }
  )

  def recalculate() = admin(
    admin =>
      Action {
        implicit request =>
          priceManagerActor ! "recalculate"
          Ok(Json.toJson(JsString("Started")))
      }
  )

  def status() = admin(
    admin =>
      Action {
        implicit request =>
          implicit val timeout = Timeout(5.seconds)
          val future = priceManagerActor ? "status"
          val result = Await.result(future, timeout.duration).asInstanceOf[String]
          Ok(Json.toJson(JsString(result)))
      }
  )


}
