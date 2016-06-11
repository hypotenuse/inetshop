package controllers.actionbuilders

import play.api.mvc._

import scala.concurrent.Future
import play.api.i18n.{Messages, Lang, I18nSupport, MessagesApi}
import play.api.mvc.Results._
class LanguageRequest[A](val lang: Lang, request: CartRequest[A]) extends WrappedRequest[A](request) {
  def cart = request.cart
  override lazy val acceptLanguages = Seq(lang)
}

object LanguageActions{
  def LanguageAction(lang: String) = new ActionRefiner[CartRequest, LanguageRequest] {
    def refine[A](input: CartRequest[A]) = Future.successful {
      import play.api.Play
      import collection.JavaConversions._
      val availLangs: List[String] = Play.current.configuration.getStringList("play.i18n.langs").get.toList
      if (!availLangs.contains(lang))
        Left {
          input.acceptLanguages.head match {
            case Lang(value, _) if availLangs.contains(value) => Redirect(controllers.routes.Application.index(value))
            case _ => Redirect(controllers.routes.Application.index(availLangs.head))
          }
        }
      else Right {
        new LanguageRequest(Lang(lang), input)
      }
    }
  }
}


