package validators

case class JOrderStatusNew(
                     title: String
                     )

case class JOrderStatusUpdate(
                        languagecod: String,
                        title: Option[String] = None,
                        sendmessageClient: Boolean,
                        sendmessageAdmin: Boolean
                        )

case class JOrderStatusEdit(
                      id: Long
                      )

case class JOrderStatusText(
                      orderstatusid: Long,
                      languageid: Long,
                      title: String
                      )








