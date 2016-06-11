package controllers.admin

import models.Admin
import play.api.mvc._
import scalikejdbc._

abstract class BaseSecuredController extends Controller {
  def isAuthenticated(implicit request:RequestHeader) =
    currentUserOpt(request).isDefined


  private def currentUserOpt(request: RequestHeader): Option[Admin] = {
    val data: Option[String] = for {
      u <- request.session.get("user").filter(_ == "admin")
      t <- request.session.get("tk")
    } yield t
    data.flatMap(t => models.Admin.findBy(sqls"sessionid = $t"))
  }

  /**
   *  Chained Action for those methods that require a valid admin.
   */
  def admin(f: Admin => EssentialAction): EssentialAction = {
    Security.Authenticated(
      currentUserOpt,
      r => {
        implicit val req = r
        Redirect(routes.Auth.login("GET")).removingFromSession("user", "tk").flashing("redirect" -> r.uri)
      }
    )(f)
  }
}