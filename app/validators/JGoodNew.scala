package validators

case class JGoodNew(
                     title: String
                     )

case class JGoodUpdate(
                        languagecod: String,
                        title: Option[String] = None,
                        description: Option[String] = None,
                        descriptionShort: Option[String] = None,
                        metatitle: Option[String] = None,
                        partnumber: Option[String] = None,
                        metadescription: Option[String] = None,
                        cost: Option[BigDecimal] = None,
                        manufacturer: Option[Long] = None,
                        warranty: Option[Int] = None,
                        import_short_desc: Option[Boolean] = None,
                        newg: Option[Boolean] = None,
                        top: Option[Boolean] = None,
                        slug: Option[String] = None
                        )

case class JGoodEdit(
                      id: Long,
                      partnumber: Option[String],
                      cost: Option[BigDecimal],
                      manufacturer: Option[Long],
                      warranty: Option[Int],
                      import_short_desc: Boolean,
                      newg: Boolean,
                      top: Boolean,
                      slug: String
                      )

case class JGoodText(
                      goodid: Long,
                      languageid: Long,
                      title: String,
                      description: Option[String] = None,
                      descriptionShort: Option[String] = None,
                      metatitle: Option[String] = None,
                      metadescription: Option[String] = None
                      )

case class JGoodPriceUpdate(
                             category: Option[Long] = None,
                             manufacturer: Option[Long] = None,
                             costfrom: Option[Int] = None,
                             costto: Option[Int] = None,
                             formula: String,
                             search: Option[String]
                             )

case class JGoodAddFromRaw(
                            title: String,
                            description: Option[String] = None,
                            descriptionShort: Option[String] = None,
                            manufacturer: Option[Long] = None,
                            partnumber: Option[String] = None,
                            categories: List[Long]
                            )


