package validators

case class JManufacturerNew(
          title: String
        )

case class JManufacturerUpdate(
         languagecod: String,
         title: Option[String] = None,
         description: Option[String] = None
       )



