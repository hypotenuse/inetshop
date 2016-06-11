package services

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import traits.{HaveThumb, HavePicture}


object ImageService {
  def generateThumb(pictureData: Array[Byte],
                    path: String,
                    width: Int,
                    height: Int) = {
    import com.sksamuel.scrimage._
    import com.sksamuel.scrimage.nio.JpegWriter
    import java.io.File

    implicit val writer = JpegWriter().withCompression(100).withProgressive(true)
    val image = Image(pictureData).fit(width, height)
    image.output(new File(path))
  }

  def generatePicture(pictureData: Array[Byte], path: String, extension: String) = {
    import java.io.File
    val inputStream = new ByteArrayInputStream(pictureData)
    val image: BufferedImage = ImageIO.read(inputStream)
    ImageIO.write(image, extension, new File(path))
  }

  def getThumbContent[T <: HavePicture with HaveThumb](entity: T, width: Int, height: Int): Option[Array[Byte]] = {
      getThumbContent(entity, width, height, entity.picture)
  }

  def getThumbContent[T <: HaveThumb](entity: T, width: Int, height: Int, data: Option[Array[Byte]]): Option[Array[Byte]] = {
    data.flatMap { d =>
      import java.io.File
      import play.Logger
      new File(entity.thumbDirPath()).mkdirs()
      FileService.getBinaryContent(entity.thumbPath()).getOrElse {
        try {
          generateThumb(d, entity.thumbPath(), width = width, height = height)
        }
        catch {
          case e: Throwable =>
            Logger.debug("Can't generate thumb in path:" + entity.thumbDirPath())
        }
      }
      FileService.getBinaryContent(entity.thumbPath())
    }
  }

  def getPictureContent[T <: HavePicture](extension: String, entity: T): Option[Array[Byte]] = {
    import java.io.File
    new File(entity.pictureDirPath()).mkdirs()
    val picture: Option[(String, Array[Byte])] = entity.picture.map(p => (entity.pictureExtension, p))

    picture.map {
      p =>
        val (ext, data) = p
        if (ext == extension) {
          FileService.getBinaryContent(entity.picturePath())
            .map { content =>
              // we found the image
              Some(content)
            }.getOrElse {
            generatePicture(data, entity.picturePath(), ext)
            FileService.getBinaryContent(entity.picturePath())
              .map { content =>
                // we found the image
                Some(content)
              }.getOrElse(None)
          }
        }
        else {
          None
        }
    }.getOrElse(None)
  }


}
