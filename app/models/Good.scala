package models

import play.Logger
import scalikejdbc._
import services.{Repository, SlugifyService}
import validators.{JGoodAddFromRaw, JGoodUpdate, JGoodNew}

import scala.util.{Failure, Success, Try}

case class Good(
                 id: Long,
                 partnumber: Option[String] = None,
                 cost: BigDecimal,
                 supplierid: Option[Long] = None,
                 suppliercost: Option[BigDecimal] = None,
                 manufacturer: Option[Long] = None,
                 import_short_desc: Boolean = true,
                 top: Boolean = false,
                 newg: Boolean = false,
                 warranty: Option[Int] = None,
                 slug: String) {

  def save()(implicit session: DBSession = Good.autoSession): Good = Good.save(this)(session)

  def destroy()(implicit session: DBSession = Good.autoSession): Unit = Good.destroy(this)(session)

  def texts()(implicit session: DBSession = Good.autoSession): List[GoodText] = Good.getTexts(this)(session)

  def textByLang(languageid: Long)(implicit session: DBSession = Good.autoSession): Option[GoodText] = Good.getTextByLang(this, languageid)(session)

  def addPicture(picture: Picture)(implicit session: DBSession = Good.autoSession): Boolean = Good.addPicture(this, picture)(session)

  def addCategory(category: Category)(implicit session: DBSession = Good.autoSession): Boolean = Good.addCategory(this, category)(session)

  def removeCategories()(implicit session: DBSession = Good.autoSession): Boolean = Good.removeCategories(this)(session)

  def categories()(implicit session: DBSession = Good.autoSession): List[Category] = Good.categories(this)(session)

  def removePicture(picture: Picture)(implicit session: DBSession = Good.autoSession): Boolean = Good.removePicture(this, picture)(session)

  def pictures(implicit session: DBSession = Good.autoSession): List[Picture] = Good.pictures(this)(session)

  def picturesIds(implicit session: DBSession = Good.autoSession): List[Long] = Good.picturesIds(this)(session)

  def picturesWithoutData(implicit session: DBSession = Good.autoSession): List[(Long, String)] = Good.picturesWithoutData(this)(session)


}


object Good extends SQLSyntaxSupport[Good] {

  override val tableName = "goods"

  override val columns = Seq("id", "partnumber", "cost", "supplierid", "suppliercost", "manufacturer", "slug", "warranty", "import_short_desc", "newg", "top")

  def apply(g: SyntaxProvider[Good])(rs: WrappedResultSet): Good = apply(g.resultName)(rs)

  def apply(g: ResultName[Good])(rs: WrappedResultSet): Good = new Good(
    id = rs.get(g.id),
    partnumber = rs.get(g.partnumber),
    cost = rs.get(g.cost),
    supplierid = rs.get(g.supplierid),
    suppliercost = rs.get(g.suppliercost),
    manufacturer = rs.get(g.manufacturer),
    import_short_desc = rs.get(g.import_short_desc),
    top = rs.get(g.top),
    newg = rs.get(g.newg),
    warranty = rs.get(g.warranty),
    slug = rs.get(g.slug)
  )

  def apply(rs: WrappedResultSet) = new Good(
    id = rs.get("id"),
    partnumber = rs.get("partnumber"),
    cost = rs.get("cost"),
    supplierid = rs.get("supplierid"),
    suppliercost = rs.get("suppliercost"),
    import_short_desc = rs.get("import_short_desc"),
    manufacturer = rs.get("manufacturer"),
    top = rs.get("top"),
    newg = rs.get("newg"),
    warranty = rs.get("warranty"),
    slug = rs.get("slug")
  )

  val g = Good.syntax("g")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Good] = {
    withSQL {
      select.from(Good as g).where.eq(g.id, id)
    }.map(Good(g.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Good] = {
    withSQL(select.from(Good as g)).map(Good(g.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Good as g)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Good] = {
    withSQL {
      select.from(Good as g).where.append(where)
    }.map(Good(g.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Good] = {
    withSQL {
      select.from(Good as g).where.append(where)
    }.map(Good(g.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Good as g).where.append(where)
    }.map(_.long(1)).single.apply().get
  }


  def create(
              title: String,
              partnumber: Option[String] = None,
              metadescription: Option[String] = None,
              cost: Option[BigDecimal] = None,
              supplierid: Option[Long] = None,
              suppliercost: Option[BigDecimal] = None,
              manufacturer: Option[Long] = None,
              import_short_desc: Boolean = true,
              top: Boolean = false,
              newg: Boolean = false,
              warranty: Option[Int] = None,
              slugstring: Option[String] = None)(implicit session: DBSession = autoSession): Good = {

    val slug = if (slugstring.isDefined)
      SlugifyService.slugify(slugstring.get, findBy(sqls"slug = ${SlugifyService.slugify(slugstring.get)}").isDefined)
    else
      SlugifyService.slugify(title, findBy(sqls"slug = ${SlugifyService.slugify(title)}").isDefined)
    val costReal: BigDecimal = cost.getOrElse(0)
    val generatedKey = withSQL {
      insert.into(Good).columns(
        column.partnumber,
        column.cost,
        column.supplierid,
        column.suppliercost,
        column.manufacturer,
        column.warranty,
        column.import_short_desc,
        column.newg,
        column.top,
        column.slug
      ).values(
        partnumber,
        costReal,
        supplierid,
        suppliercost,
        manufacturer,
        warranty,
        import_short_desc,
        newg,
        top,
        slug
      )
    }.updateAndReturnGeneratedKey.apply()

    Good(
      id = generatedKey,
      partnumber = partnumber,
      cost = costReal,
      supplierid = supplierid,
      suppliercost = suppliercost,
      manufacturer = manufacturer,
      warranty = warranty,
      import_short_desc = import_short_desc,
      newg = newg,
      top = top,
      slug = slug)
  }

  def create(jGood: JGoodNew): Good = {
    val good = create(
      title = jGood.title
    )
    Language.findBy(sqls"defaultlng = true").map {
      l =>
        GoodText.create(good.id, l.id, jGood.title)
    }.getOrElse {
      throw new Exception("Can't find default language in database!")
    }

    good
  }

  def create(jGood: JGoodAddFromRaw): Good = {
    val good = create(
      title = jGood.title
    )
    Language.findBy(sqls"defaultlng = true").map {
      l =>
        GoodText.create(
          goodid = good.id,
          languageid = l.id,
          title = jGood.title,
          description = jGood.description,
          descriptionShort = jGood.descriptionShort
        )
        val categories: List[Category] = jGood.categories.flatMap(Category.find(_))
        good.removeCategories()
        categories.map(good.addCategory(_))
    }.getOrElse {
      throw new Exception("Can't find default language in database!")
    }

    good
  }

  def save(goodId: Long, jGood: JGoodUpdate): Good = {
    val good = Good.find(goodId)
    val lang = Language.getByCod(jGood.languagecod).getOrElse {
      throw new Exception("Can't find language with code ${jGood.languagecod} in database!")
    }

    good.map {
      g =>
        GoodText.find(g.id, lang.id).map {
          t =>
            t.copy(
              t.goodid,
              t.languageid,
              jGood.title.getOrElse(t.title),
              jGood.description.orElse(t.description),
              jGood.descriptionShort.orElse(t.descriptionShort),
              jGood.metatitle.orElse(t.metatitle),
              jGood.metadescription.orElse(t.metadescription)
            ).save()

        }.getOrElse {
          jGood.title.map {
            t =>
              GoodText.create(
                goodid = g.id,
                languageid = lang.id,
                title = t,
                description = jGood.description,
                descriptionShort = jGood.descriptionShort,
                metatitle = jGood.metatitle,
                metadescription = jGood.metadescription
              )
          }.getOrElse(throw new IllegalArgumentException(s"Can't create text for language ${lang.cod} with empty title"))

        }
        val newManufacturer = jGood.manufacturer match {
          case Some(0) => None
          case Some(v) => Some(v)
          case None => g.manufacturer
        }
        g.copy(
          id = g.id,
          partnumber = jGood.partnumber.orElse(g.partnumber),
          cost = jGood.cost.getOrElse(g.cost),
          supplierid = g.supplierid.orElse(g.supplierid),
          suppliercost = g.suppliercost.orElse(g.suppliercost),
          manufacturer = newManufacturer,
          warranty = jGood.warranty.orElse(g.warranty),
          import_short_desc = jGood.import_short_desc.getOrElse(g.import_short_desc),
          newg = jGood.newg.getOrElse(g.newg),
          top = jGood.top.getOrElse(g.top),
          slug = jGood.slug.getOrElse(g.slug)
        ).save()

    }.getOrElse(throw new NoSuchElementException("Good does not exist"))

  }

  def save(entity: Good)(implicit session: DBSession = autoSession): Good = {
    withSQL {
      update(Good).set(
        column.id -> entity.id,
        column.partnumber -> entity.partnumber,
        column.cost -> entity.cost,
        column.supplierid -> entity.supplierid,
        column.suppliercost -> entity.suppliercost,
        column.manufacturer -> entity.manufacturer,
        column.import_short_desc -> entity.import_short_desc,
        column.newg -> entity.newg,
        column.top -> entity.top,
        column.warranty -> entity.warranty,
        column.slug -> entity.slug
      ).where.eq(column.id, entity.id)
    }.update.apply()
    entity
  }

  def destroy(entity: Good)(implicit session: DBSession = autoSession): Unit = {
    entity.pictures.foreach(_.destroy())
    withSQL {
      delete.from(GoodText).where.eq(sqls"goodid", entity.id)
    }
    withSQL {
      delete.from(Good).where.eq(column.id, entity.id)
    }.update.apply()
  }

  def getTexts(entity: Good)(implicit session: DBSession = autoSession): List[GoodText] = {
    GoodText.findAllBy(sqls"goodid = ${entity.id}")
  }

  def getTextByLang(entity: Good, languageid: Long)(implicit session: DBSession = autoSession): Option[GoodText] = {
    GoodText.findBy(sqls"goodid = ${entity.id} and languageid = ${languageid}")
  }

  def addPicture(good: Good, picture: Picture)(implicit session: DBSession = autoSession): Boolean = {
    def insert(): Boolean = {
      sql"""insert into good_picture(pictureid, goodid)
               values (${picture.id}, ${good.id})"""
        .updateAndReturnGeneratedKey().apply()
      true
    }
    Repository.contains("good_picture", s"goodid=${good.id} AND pictureid=${picture.id}") match {
      case Success(x) if !x => insert()
      case Failure(t) => throw t
      case _ => true
    }
  }

  def removePicture(good: Good, picture: Picture)(implicit session: DBSession = autoSession): Boolean = {
    implicit val session = AutoSession
    def remove(): Boolean = {
      sql"""DELETE FROM good_picture WHERE goodid = ${good.id} AND pictureid=${picture.id}"""
        .update().apply()
      true
    }
    Repository.contains("good_picture", s"goodid=${good.id} AND pictureid=${picture.id}") match {
      case Success(x) if x => remove()
      case Failure(t) => throw t
      case _ => true
    }
  }

  def pictures(good: Good)(implicit session: DBSession = autoSession): List[Picture] = {
    implicit val session = AutoSession
    DB readOnly { implicit session =>
      sql"select * from good_picture JOIN pictures on pictures.id=good_picture.pictureid WHERE goodid=${good.id}"
        .map(rs => Picture(rs)).list().apply()
    }
  }

  def picturesIds(good: Good)(implicit session: DBSession = autoSession): List[Long] = {
    implicit val session = AutoSession
    DB readOnly { implicit session =>
      sql"select pictureid from good_picture WHERE goodid=${good.id}"
        .map(rs => rs.long(1)).list().apply()
    }
  }

  def picturesWithoutData(good: Good)(implicit session: DBSession = autoSession): List[(Long, String)] = {
    implicit val session = AutoSession
    DB readOnly { implicit session =>
      sql"select pictureid, extension from good_picture JOIN pictures ON pictureid = pictures.id WHERE goodid=${good.id}"
        .map(rs => (rs.long("pictureid"), rs.string("extension"))).list().apply()
    }
  }

  def addCategory(good: Good, category: Category)(implicit session: DBSession = Good.autoSession): Boolean = {
    def insert(): Boolean = {
      sql"""insert into good_category(categoryid, goodid)
               values (${category.id}, ${good.id})"""
        .update().apply()
      true
    }
    Repository.contains("good_category", s"goodid=${good.id} AND categoryid=${category.id}") match {
      case Success(x) if !x => insert()
      case Failure(t) => throw t
      case _ => true
    }
  }

  def removeCategories(good: Good)(implicit session: DBSession = Good.autoSession): Boolean = {
    implicit val session = AutoSession
    def remove(): Boolean = {
      sql"""DELETE FROM good_category WHERE goodid = ${good.id}"""
        .update.apply()
      true
    }
    Repository.contains("good_category", s"goodid=${good.id}") match {
      case Success(x) if x => remove()
      case Failure(t) => throw t
      case _ => true
    }

  }

  def categories(good: Good)(implicit session: DBSession = Good.autoSession): List[Category] = {
    def list(): List[Category] = {
      val result = DB readOnly { implicit session =>
        sql"select * from good_category WHERE goodid=${good.id}"
      }
      result.map(res => Category.find(res.long("categoryid"))).list().apply().flatten
    }
    Repository.contains("good_category", s"goodid=${good.id}") match {
      case Success(x) if x => list()
      case Failure(t) => throw t
      case _ => List()
    }

  }


}
