package controllers.admin

import play.api.mvc.Action
import play.api.routing.JavaScriptReverseRouter


class Home extends BaseSecuredController{
  def index() = admin(
   admin =>
    Action { request =>
      Ok(views.html.admin.home(admin.name))
    }
  )

  def renderurl(url: String) = admin(
    admin =>
      Action { request =>
        Ok(views.html.admin.home(admin.name))
      }
  )

  def javascriptRoutes = admin(
  admin =>
    Action { implicit request =>
      Ok(
        JavaScriptReverseRouter("jsRoutes")(
          routes.javascript.Goods.add
        )
      ).as("text/javascript")
    }
  )
}
