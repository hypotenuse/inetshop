package actors

import akka.actor.{ActorRef, Props, Actor}


class PriceManagerActor () extends Actor {

  var price: ActorRef = _
  var status = "Idle"



  implicit val ec = context.dispatcher

  override def preStart(): Unit = {
    price = context.actorOf(Props[PriceActor], name = "price")
  }
  def receive = {
    case "recalculate" =>
      price ! "recalculate"
      status = "Working"
    case "status" => sender() ! status
    case "recalculated" => status = "Idle"
  }
}
