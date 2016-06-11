package actors

import java.io.PrintWriter
import javax.inject.Inject
import messages._
import messages.{Status => StatusForClient}
import models.Supplier
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json

import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor._

import scala.reflect.io.File

class ExcelManagerActor @Inject()(app: play.api.Application) extends Actor {

  var db: ActorRef = _
  var excel: ActorRef = _
  var statuses: List[PriceStatus] = List()
  var histories: List[History] = List()
  var statusesExcelProcessed: List[PriceStatus] = List()
  val pricesDir = app.path + app.configuration.getString("prices.path").get
  val statusFile = s"${pricesDir}status.txt"


  implicit val ec = context.dispatcher

  override def preStart(): Unit = {
    db = context.actorOf(Props[DbActor], name = "db")
    excel = context.actorOf(Props[ExcelActor], name = "excel")
    context.system.scheduler.scheduleOnce(1.seconds, self, "start")
    //    context.watch(excel)
  }

  def receive = stopped

  def stopped: Receive = {
    case "start" =>
//      println("Starting ExcelManagerActor ...")
      import scala.io.Source
      if (File(statusFile).exists) {
        val status = Source.fromFile(statusFile).getLines().toList.headOption
        status.foreach { s =>
          val json = Json.parse(s)
          statuses = json.as[List[NewPrice]].map(PriceStatus(_, "Waiting", 0))

        }
        statuses.foreach(s => db ! CleanData(s.price.supplierId))
      }

//      println(statuses.toString())
      context.system.scheduler.scheduleOnce(5.seconds, self, "process")
      context.become(excelProcessing)
    case "status" => statusForClient()
  }

  def excelProcessing: Receive = {
    case "stop" =>
//      println("Stopping ExcelManagerActor ...")
      context.become(stopped)
    case price: NewPrice => newPrice(price)

    case "process" =>
//      println("Processing ExcelManagerActor ...")
//      println(statuses.toString())
      val toProcess = statuses.filter(_.status == "Waiting")
      val alreadyProcessing = statuses.filter(_.status == "Excel_processing")
      if (toProcess.nonEmpty && alreadyProcessing.isEmpty) {
        val current = toProcess.reverse.head
        val modifiedCurrent = current.copy(status = "Excel_processing")
        updateStatus(modifiedCurrent)
        excel ! modifiedCurrent.price
      }
      context.system.scheduler.scheduleOnce(5.seconds, self, "process")
    case "status" => statusForClient()
    case result: ExcelResult =>
      val current = statuses.filter(_.status == "Excel_processing").head
      val modifiedCurrent = current.copy(processed = current.processed + 1)
      updateStatus(modifiedCurrent)
      db ! result
    case "ExcelFinish" =>
      db ! "PriceFinish"
      context.become(dbInsert)
//      println("PriceFinish go to dbInsert")
    case "PriceFinish" => priceFinish()
  }


  def dbInsert: Receive = {
    case price: NewPrice => newPrice(price)
    case "PriceFinish" => priceFinish()
    case "status" => statusForClient()
  }

  def priceUpdate: Receive = {
    case price: NewPrice => newPrice(price)
    case "status" => statusForClient()
    case "process" =>
      val toProcess = statuses.filter(_.status == "Excel_Processed_Waiting").reverse
      val modifiedCurrent = toProcess.map(c => c.copy(status = "DB_updating"))
      modifiedCurrent.foreach{mc =>
        updateStatus(mc)
      }
      toProcess.foreach{c =>
        println("Sending: " + UpdatePrice(c.price.supplierId, c.price.filename))
        db ! UpdatePrice(c.price.supplierId, c.price.filename)
      }

    case updated: PriceUpdated =>
      val updStatus = statuses.filter(_.price.filename == updated.file).head
      statuses = statuses.filterNot(_.price.filename == updated.file)
      val left = statuses.filter(s => s.status == "DB_updating")
      val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
      val now = DateTime.now()
      Supplier.find(updated.supplierId).foreach(s =>
        histories = (History(format.print(now), s.title , updStatus.processed ) :: histories).take(5)
      )
      if(left.isEmpty){
        context.system.scheduler.scheduleOnce(5.seconds, self, "process")
        context.become(excelProcessing)
      }
  }

  private def newPrice(price: NewPrice)={
    statuses = PriceStatus(price, "Waiting", 0) :: statuses
    writeStatus()
    db ! CleanData(price.supplierId)
  }

  private def writeStatus()={
    val writer = new PrintWriter(statusFile)
    writer.println(Json.toJson(statuses.map(s => s.price)))
    writer.close()
  }

  private def updateStatus(modifiedCurrent: PriceStatus)={
    statuses = statuses.map{case v => if (v.price.filename == modifiedCurrent.price.filename) modifiedCurrent else v}
  }

  private def priceFinish() = {
//    println("Received finish from DB actor")
    val current = statuses.filter(_.status == "Excel_processing").head
    val modifiedCurrent = current.copy(status = "Excel_Processed_Waiting")
    updateStatus(modifiedCurrent)
    val waitingToProcess = statuses.filter(_.status == "Waiting")
    if (waitingToProcess.isEmpty) {
      System.gc()
      context.become(priceUpdate)
    }
    else {
      context.become(excelProcessing)
    }
    self ! "process"
  }

  def statusForClient()={
    val statusesFormatted: List[StatusForClient] = for {
      s <- statuses
      sup <- Supplier.find(s.price.supplierId)
    } yield StatusForClient(s.status, sup.title, s.processed)
    sender() ! Statuses(statusesFormatted, histories)
  }
}
