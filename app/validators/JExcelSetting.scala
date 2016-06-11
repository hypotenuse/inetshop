package validators

case class CurrencyIdSign(currency: Long, sign: String)
case class CategoryColumn(column: Int)

case class JExcelSetting(
                          supplier: Long,
                          titleColumn: Int,
                          sheetNumber: Int,
                          title: String,
                          costColumn: Int,
                          costCurrencyId: Option[Long],
                          costRrzColumn: Option[Int] = None,
                          partnumberColumn: Option[Int] = None,
                          warrantyColumn: Option[Int] = None,
                          brandColumn: Option[Int] = None,
                          goodidSupplierColumn: Option[Int] = None,
                          categoryColumns: Option[List[CategoryColumn]] = None,
                          descriptionColumn: Option[Int] = None,
                          imageUrlColumn: Option[Int] = None,
                          warrantyInMonths: Option[Boolean] = None,
                          modelColumn: Option[Int] = None,
                          amountColumn: Int,
                          currencySignColumn: Option[Int] = None,
                          currencyIdSign: Option[List[CurrencyIdSign]] = None,
                          costRrzCurrencyId: Option[Long] = None
                          )



