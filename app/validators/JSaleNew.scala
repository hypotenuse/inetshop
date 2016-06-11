package validators

case class JSaleNew(
          title: String,
          titlecolorbackgrnd: String
        )

case class JSaleUpdate(
         languagecod: String,
         title: Option[String] = None,
         text: Option[String] = None,
         titlecolorbackgrnd: Option[String] = None
       )



