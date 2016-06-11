package repositories


case class GoodListAggregate(
                              id: Long,
                              title: String,
                              partnumber: Option[String],
                              manufacturerid: Option[Long],
                              manufacturerTitle: Option[String],
                              cost: Option[java.math.BigDecimal],
                              supplierСost: Option[java.math.BigDecimal],
                              warranty: Option[Int],
                              supplierid: Option[Long],
                              supplier: Option[String],
                              import_short_desc: Boolean
                              )

case class GoodFrontendAggregate(
                              id: Long,
                              title: String,
                              partnumber: Option[String],
                              manufacturerTitle: Option[String],
                              cost: BigDecimal,
                              warranty: Option[Int],
                              short_desc: Option[String],
                              picture: Option[Long],
                              slug: String
                              )

