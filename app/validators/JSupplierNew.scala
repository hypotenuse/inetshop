package validators

case class JSupplierNew(
                         title: String
                         )

case class JSupplierUpdate(
                            title: Option[String] = None,
                            info: Option[String] = None,
                            currencies: Option[List[JSupplierCurrency]] = None
                            )

case class JSupplierCurrency(
                              id: Long,
                              rate: Option[BigDecimal] = None
                              )



