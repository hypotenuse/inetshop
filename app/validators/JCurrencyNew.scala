package validators

case class JCurrencyNew(
          title: String, main: Boolean, cros: BigDecimal
        )


case class JCurrencyUpdate(
                        title: Option[String], main: Option[Boolean], cros: Option[BigDecimal]
                         )
