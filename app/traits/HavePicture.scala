package traits

import services.FileService

/**
 * Picture management on server filesystem
 */
trait HavePicture {
  val id: Long
  val pictureDir: String
  val picture: Option[Array[Byte]]

  def pictureDirPath()={
    import play.api.Play.current
    import play.api.Play
    Play.application(current).path.getAbsolutePath + current.configuration.getString("images.path").get + pictureDir + "/"
  }

  def picturePath()={
    pictureDirPath + id + "." + pictureExtension
  }

  def deletePicture()={
    FileService.deleteFile(picturePath())
  }

  def pictureUrl(baseUrl: String): String
  def pictureExtension : String
}
