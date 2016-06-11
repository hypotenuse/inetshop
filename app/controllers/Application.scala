package controllers

import javax.inject.Inject
import controllers.actionbuilders._
import controllers.actionbuilders.LanguageActions._
import models.Manufacturer
import play.api.Logger
import play.api.mvc._
import repositories.{GoodSearch, GoodFrontendAggregate, GoodListAggregate}
import services.Repository
import services.auth.Authenticator
import play.api.Play._
import play.api.i18n.{Messages, Lang, I18nSupport, MessagesApi}

import scala.util.{Success, Failure}


class Application @Inject()(val messagesApi: MessagesApi, authServ: Authenticator) extends Controller with I18nSupport {

  def index(lng: String) = (CartAction andThen LanguageAction(lng)) {
    implicit request =>
    import scalikejdbc._
    import models.{Language, Category, Good, News, Setting}
    val langv: Language = models.Language.getByCod(request.lang.language).getOrElse(models.Language.getDefault.getOrElse(throw new NoSuchElementException("No default language configured")))
    val cats = Category.findAllBy(sqls"onhome = True")
    val leaders = GoodSearch.frontendAggregate(langv, Good.findAllBy(sqls"top = True"))
    val newg = GoodSearch.frontendAggregate(langv, Good.findAllBy(sqls"newg = True"))
    val news = News.findLast(5)
    val settings = Setting.findAll()
    val metatitle = settings.find(s => s.shortcode == "hometitle" + langv.cod.head.toUpper + langv.cod.tail)
    val metadescription = settings.find(s => s.shortcode == "homedescription" + langv.cod.head.toUpper + langv.cod.tail)
    val bottomText = settings.find(s => s.shortcode == "bottomText" + langv.cod.head.toUpper + langv.cod.tail)
    Ok(views.html.index(cats, leaders, newg, langv, metadescription.map(_.value) getOrElse "", metatitle.map(_.value) getOrElse "", news, bottomText.map(_.value) getOrElse ""))
  }

  def home() = Action {
    implicit request =>
      import play.api.Play
      import collection.JavaConversions._
      val availLangs: List[String] = Play.current.configuration.getStringList("play.i18n.langs").get.toList
      request.acceptLanguages.head match {
        case Lang(value, _) if availLangs.contains(value) => Redirect(controllers.routes.Application.index(value))
        case _ => Redirect(controllers.routes.Application.index(availLangs.head))
      }
  }

  def cat(lng: String, slug: String) = (CartAction andThen LanguageAction(lng)) {
    implicit request =>
    import scalikejdbc._
    import models.{Language, Category, Good}
    val cats = Category.findAllBy(sqls"onhome = True")
    val tops = Good.findAllBy(sqls"top = True")
    val newg = Good.findAllBy(sqls"newg = True")
    val langv: Language = models.Language.getByCod(request.lang.language).getOrElse(models.Language.getDefault.getOrElse(throw new NoSuchElementException("No default language configured")))
    Ok("Ok")
  }



}
