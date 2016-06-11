package messages

case class ExcelResult(supplierId: Long,
                       settingId: Long,
                       filename: String,
                       excelString: ExcelString,
                       currencyId: Long)

case class ExcelString(
                        title: String,
                        cost: BigDecimal,
                        costRrz: Option[BigDecimal] = None,
                        partnumber: Option[String] = None,
                        warranty: Option[Int] = None,
                        brand: Option[String] = None,
                        supplierGoodid: Option[String] = None,
                        category: Option[String] = None,
                        description: Option[String] = None,
                        imageUrl: Option[String] = None,
                        model: Option[String] = None
                        )
