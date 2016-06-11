package modules

import play.api.libs.concurrent.AkkaGuiceSupport
import com.google.inject._
import akka.actor.{Props, ActorRef, ActorSystem}
import com.google.inject.name.Named
import actors.{PriceManagerActor, ExcelManagerActor}

class ActorsModule extends AbstractModule with AkkaGuiceSupport {
  def configure() = {
    //    bindActor[ManagerActor]("manager-actor")
    bind(classOf[Actors]).asEagerSingleton()
    @Provides
    @Named("excel-manager-actor")
    def excelManagerActor(actors: Actors): ActorRef = actors.excelManagerActor

    bind(classOf[Actors]).asEagerSingleton()
    @Provides
    @Named("price-manager-actor")
    def priceManagerActor(actors: Actors): ActorRef = actors.priceManagerActor
  }
}

@Singleton
class Actors @Inject() (system: ActorSystem, app: play.api.Application) {
  val excelManagerProps = Props(classOf[ExcelManagerActor], app)
  val priceManagerProps = Props(classOf[PriceManagerActor])
  //  val managerActor = system.actorOf(Props(injector.getInstance(classOf[MyActor])), "myactor")
  val priceManagerActor = system.actorOf(priceManagerProps, name = "price-manager-actor")
  val excelManagerActor = system.actorOf(excelManagerProps, name = "excel-manager-actor")
}

