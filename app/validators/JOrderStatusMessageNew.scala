package validators

case class JOrderStatusMessageNew(
                                   orderstatusid: Long,
                                   messagetitle: String,
                                   messagetext: String,
                                   forclient: Boolean
        )


case class JOrderStatusMessageUpdate(
                                      orderstatusid: Long,
                                      languagecod: String,
                                      messagetitle: Option[String],
                                      messagetext: Option[String],
                                      forclient: Boolean
                         )
