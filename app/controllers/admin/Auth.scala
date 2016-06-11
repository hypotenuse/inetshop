package controllers.admin

import javax.inject.Inject

import models.Admin
import formvalidators.AdminValidator
import play.Logger
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{Messages, Lang, I18nSupport, MessagesApi}
import play.filters.csrf._
import services.auth.Authenticator
import play.api.mvc.Security.Authenticated



import scala.util.Success

class Auth @Inject()(val messagesApi: MessagesApi, val authServ: Authenticator) extends Controller with I18nSupport {
  val adminForm = Form(mapping(
    "email" -> nonEmptyText,
    "password" -> nonEmptyText)(AdminValidator.apply)(AdminValidator.unapply))


  def login(method: String) = CSRFCheck {
    Action {
    implicit request =>
    implicit val token = CSRF.getToken(request)
      method match {
        case "POST" =>
          adminForm.bindFromRequest.fold(
            formWithErrors => Ok(views.html.admin.login(formWithErrors)),
            value => authServ.adminAuth(value.email, value.password) match {
              case Some(m) =>
                request.session.get("redirect").map{
                  red =>
                    Redirect(red).withSession("user" -> "admin", "tk" -> m.sessionid.get)
                }.getOrElse(
                    Redirect(controllers.admin.routes.Home.index).withSession("user" -> "admin", "tk" -> m.sessionid.get)
                  )
              case _ => Ok(views.html.admin.login(adminForm.withGlobalError(Messages("incorrect.data"))))
            }
          )
        case "GET" => request.session.get("user").map{
          _ => Redirect(controllers.admin.routes.Home.index)
        }.getOrElse{
          val messages = new Messages(Lang("ru"), messagesApi)
          Ok(views.html.admin.login(adminForm)(messages, token.get))
        }
      }
    }
  }
}
