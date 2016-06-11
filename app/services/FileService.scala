package services

import java.io.IOException

import play.api.{Logger, Play}
import scala.io.{BufferedSource, Codec, Source}
import play.api.Play.current


object FileService {

  val tempPath = Play.application(current).path.getAbsolutePath + "/tmp/"

  def getTextContent(path: String):Option[String]={
    val codec = scala.io.Codec.UTF8
    getContent(path, codec,fileDesc=>fileDesc.mkString)
  }

  def getBinaryContent(path: String):Option[Array[Byte]]={
    val codec = scala.io.Codec.ISO8859
    getContent(path, codec,fileDesc=>fileDesc.map(_.toByte).toArray)
  }

  def getContentType(extention: String) : String = extention match {
    case "jpg" => "image/jpeg"
    case "gif" => "image/gif"
    case "css" => "text/css"
    case "js" => "application/javascript"
    case "png" => "image/png"
    case "ico" => "image/ico"
    case _ => "text/html"
  }

  def getExtension(contentType: String) : String = contentType match {
    case  "image/jpeg" => "jpg"
    case  "image/gif" => "gif"
    case  "text/css" => "css"
    case  "application/javascript" => "js"
    case  "image/png" => "png"
    case  "image/ico" => "ico"
    case _ => "text/html"
  }

  private def getContent[T](path: String, codec: Codec, f: BufferedSource=>T):Option[T]={
    try {
      val fileSource = Source.fromFile(path)(codec)
      val result = Some(f(fileSource))
      fileSource.close()
      result
    }
    catch
      {
        case _ : Throwable =>
          None
      }
  }

  def deleteFile(path: String): Unit ={
    import java.io.File
    try{
      new File(path).delete()
    }
    catch {
      case e @ (_: SecurityException | _: IOException )=> Logger.debug(s"Can't delete picture ${path}")
    }
  }
}
