package validators

case class JAdminNew(
                      name: String,
                      email: String,
                      pass: String
                      )

case class JAdminUpdate(
                        name: Option[String] = None,
                        email: Option[String] = None,
                        pass: Option[String] = None,
                        oldpass: Option[String] = None
                        )



