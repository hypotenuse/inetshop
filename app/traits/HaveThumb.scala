package traits

import services.FileService

/**
 * Thumb management on server filesystem
 */
trait HaveThumb{
  val id: Long
  val pictureDir: String

  def thumbDirPath()={
    import play.api.Play.current
    import play.api.Play
    Play.application(current).path.getAbsolutePath + current.configuration.getString("thumbnail.path").get + pictureDir + "/"
  }

  def thumbPath()={
    thumbDirPath + id + ".jpg"
  }

  def deleteThumb()={
    FileService.deleteFile(thumbPath())
  }
}
