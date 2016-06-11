package services

import play.api.mvc.RequestHeader


object UrlService {
  def baseUrl(implicit request: RequestHeader): String = {
    if(request.secure) "https://" + request.host + "/" else "http://" + request.host + "/"
  }
}
