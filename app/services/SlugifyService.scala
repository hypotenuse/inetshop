package services

import java.util.UUID
import com.osinka.slugify.Slugify
import org.apache.commons.lang3.StringUtils
import scalikejdbc.interpolation.SQLSyntax

object SlugifyService {
  def slugify (string: String, addRand: Boolean = false): String={
    if(addRand){
      val uuid = UUID.randomUUID().toString
      StringUtils.substring(Slugify(string) + "-" + uuid,0,250)
    }
    else {
      StringUtils.substring(Slugify(string),0,250)
    }
  }

}
