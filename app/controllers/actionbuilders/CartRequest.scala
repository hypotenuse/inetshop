package controllers.actionbuilders

import models.{Cart, Good}
import play.api.mvc._
import scalikejdbc._

import scala.concurrent.Future

class CartRequest[A](val cart: Option[Cart], request: Request[A]) extends WrappedRequest[A](request)

object CartAction extends
ActionBuilder[CartRequest] with ActionTransformer[Request, CartRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    request.session.get("c").map{
      id =>
        Cart.find(id.toLong).map(c => new CartRequest(Some(c), request)).getOrElse(new CartRequest(None, request))
    }.getOrElse(new CartRequest(None, request))
  }
}
