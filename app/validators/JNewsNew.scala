package validators

case class JNewsNew(
          title: String
        )

case class JNewsUpdate(
         languagecod: String,
         title: Option[String] = None,
         content: Option[String] = None
       )



