package validators

case class JSettingNew(
          title: String,
          value: String,
          shortcode: String
        )

case class JSettingUpdate(
         title: Option[String] = None,
         value: Option[String] = None,
         shortcode: Option[String] = None
       )



