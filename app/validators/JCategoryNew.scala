package validators

case class JCategoryNew(
          title: String, parentId: Option[Long]
        )

case class JCategoryUpdate(
         languagecod: String,
         title: Option[String] = None,
         description: Option[String] = None,
         metatitle: Option[String] = None,
         metadescription: Option[String] = None,
         slug: Option[String] = None,
         onhome: Option[Boolean] = None,
         parentId: Option[Long] = None
       )



