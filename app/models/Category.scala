package models

import play.Logger
import scalikejdbc._
import services.{FileService, Repository, SlugifyService}
import traits.{HaveThumb, HavePicture}
import validators.{JCategoryUpdate, JCategoryNew}

import scala.util.{Failure, Success, Try}

case class Category(
                     id: Long,
                     picture: Option[Array[Byte]] = None,
                     extension: Option[String] = None,
                     parent: Option[Long] = None,
                     pathtorootids: String,
                     slug: String,
                     onhome: Boolean = false,
                     catpicture: Option[Array[Byte]] = None
                     ) extends HavePicture with HaveThumb{
  val pictureDir = "categories"


  def save()(implicit session: DBSession = Category.autoSession): Category = Category.save(this)(session)

  def children()(implicit session: DBSession = Category.autoSession): List[Category] = Category.getChildren(this)(session)

  def childrenIds()(implicit session: DBSession = Category.autoSession): List[Long] = Category.childrenIds(this)(session)

  def texts()(implicit session: DBSession = Category.autoSession): List[CategoryText] = Category.getTexts(this)(session)

  def textByLang(languageid: Long)(implicit session: DBSession = Category.autoSession): Option[CategoryText] = Category.getTextByLang(this, languageid)(session)

  def textByDefaultLang(implicit session: DBSession = Category.autoSession): Option[CategoryText] = Category.getTextByDefaultLang(this)(session)

  def addGood(good: Good)(implicit session: DBSession = Category.autoSession): Boolean = Category.addGood(this, good)(session)

  def hasAdvert()(implicit session: DBSession = Category.autoSession): Boolean = Category.hasAdvert(this)(session)

  def removeGood(good: Good)(implicit session: DBSession = Category.autoSession): Boolean = Category.removeGood(this, good)(session)

  def goods(orderby: String, offset: Int = 0, limit: Int = 10, withChildren: Boolean = false)(implicit session: DBSession = Category.autoSession): List[Good] = Category.goods(this, orderby, offset, limit, withChildren)(session)

  def pictureUrl(baseUrl: String): String = Category.pictureUrl(baseUrl, this)

  def pictureCategoryUrl(baseUrl: String): String = Category.pictureCategoryUrl(baseUrl, this)

  def destroy()(implicit session: DBSession = Category.autoSession): Try[Boolean] = Category.destroy(this)(session)

  def pictureExtension: String = Category.pictureExtension(this)
}


object Category extends SQLSyntaxSupport[Category] {

  override val tableName = "categories"

  override val columns = Seq("id", "picture", "parent", "pathtorootids", "slug", "onhome", "extension", "catpicture")

  def apply(c: SyntaxProvider[Category])(rs: WrappedResultSet): Category = apply(c.resultName)(rs)

  def apply(c: ResultName[Category])(rs: WrappedResultSet): Category = new Category(
    id = rs.get(c.id),
    picture = rs.get(c.picture),
    parent = rs.get(c.parent),
    pathtorootids = rs.get(c.pathtorootids),
    slug = rs.get(c.slug),
    onhome = rs.get(c.onhome),
    extension = rs.get(c.extension),
    catpicture = rs.get(c.catpicture)
  )

  val c = Category.syntax("c")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Category] = {
    withSQL {
      select.from(Category as c).where.eq(c.id, id)
    }.map(Category(c.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Category] = {
    withSQL(select.from(Category as c)).map(Category(c.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(Category as c)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Category] = {
    withSQL {
      select.from(Category as c).where.append(where)
    }.map(Category(c.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Category] = {
    withSQL {
      select.from(Category as c).where.append(where)
    }.map(Category(c.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(Category as c).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
              title: String,
              picture: Option[Array[Byte]] = None,
              parent: Option[Long] = None,
              slugstring: Option[String] = None,
              onhome: Boolean = false,
              extension: Option[String] = None,
              catpicture: Option[Array[Byte]] = None
              )(implicit session: DBSession = autoSession): Category = {

    val slug = if (slugstring.isDefined)
      SlugifyService.slugify(slugstring.get, findBy(sqls"slug = ${SlugifyService.slugify(slugstring.get)}").isDefined)
    else
      SlugifyService.slugify(title, findBy(sqls"slug = ${SlugifyService.slugify(title)}").isDefined)
    val generatedKey = withSQL {
      insert.into(Category).columns(
        column.picture,
        column.parent,
        column.pathtorootids,
        column.slug,
        column.onhome,
        column.extension,
        column.catpicture
      ).values(
        picture,
        parent,
        pathtorootids(parent),
        slug,
        onhome,
        extension,
        catpicture
      )
    }.updateAndReturnGeneratedKey.apply()

    Category(
      id = generatedKey,
      picture = picture,
      parent = parent,
      pathtorootids = pathtorootids(parent),
      slug = slug,
      onhome = onhome,
      extension = extension,
      catpicture = catpicture
    )
  }


  def create(jCat: JCategoryNew): Category = {
    val category = create(
      title = jCat.title,
      parent = jCat.parentId
    )
    Language.findBy(sqls"defaultlng = true").map {
      l =>
        CategoryText.create(category.id, l.id, jCat.title)
    }.getOrElse {
      throw new Exception("Can't find default language in database!")
    }

    category
  }

  def save(entity: Category)(implicit session: DBSession = autoSession): Category = {
    def updateChildren() = {
      getChildren(entity).map { c =>
        c.copy(parent = c.parent).save()
      }
    }
    def updateCatText() = {
      getTexts(entity).map(t => t.copy(title = t.title).save())
    }
    withSQL {
      update(Category).set(
        column.id -> entity.id,
        column.picture -> entity.picture,
        column.parent -> entity.parent,
        column.pathtorootids -> pathtorootids(entity.parent),
        column.slug -> SlugifyService.slugify(entity.slug, findBy(sqls"slug = ${SlugifyService.slugify(entity.slug)} AND id <> ${entity.id}").isDefined),
        column.onhome -> entity.onhome,
        column.extension -> entity.extension,
        column.catpicture -> entity.catpicture
      ).where.eq(column.id, entity.id)
    }.update.apply()
    updateCatText()
    updateChildren()
    entity
  }

  def save(catId: Long, jCat: JCategoryUpdate): Category = {
    val cat = Category.find(catId)
    val lang = Language.getByCod(jCat.languagecod).getOrElse {
      throw new Exception("Can't find language with code ${jCat.languagecod} in database!")
    }

    cat.map {
      category =>
        jCat.parentId.foreach {
          newParentId =>
            if (newParentId == catId) throw new categoryParentEqualToCategory
            if (category.childrenIds().contains(newParentId)) throw new categoryParentEqualToItsChildren
        }
        Category.getTextByLang(category, lang.id).map {
          t =>
            t.copy(
              t.catid,
              jCat.title.getOrElse(t.title),
              jCat.description.orElse(t.description),
              jCat.metatitle.orElse(t.metatitle),
              jCat.metadescription.orElse(t.metadescription),
              t.pathtoroot,
              t.languageid
            ).save()

        }.getOrElse {
          jCat.title.map {
            t =>
              CategoryText.create(
                catid = category.id,
                languageid = lang.id,
                title = t,
                description = jCat.description,
                metatitle = jCat.metatitle,
                metadescription = jCat.metadescription
              )
          }.getOrElse(throw new IllegalArgumentException(s"Can't create text for language ${lang.cod} with empty title"))

        }
        val newParent = jCat.parentId match {
          case Some(0) => None
          case Some(v) => Some(v)
          case None => category.parent
        }
        category.copy(
          id = category.id,
          picture = category.picture,
          parent = newParent,
          pathtorootids = category.pathtorootids,
          onhome = jCat.onhome.getOrElse(category.onhome),
          slug = jCat.slug.getOrElse(category.slug),
          extension = category.extension,
          catpicture = category.catpicture
        ).save()

    }.getOrElse(throw new NoSuchElementException("Category does not exist"))

  }

  def destroy(entity: Category)(implicit session: DBSession = autoSession): Try[Boolean] = {
    if (getChildren(entity).nonEmpty) Failure(new IllegalStateException("Category has children"))
    else {
      withSQL {
        delete.from(Category).where.eq(column.id, entity.id)
      }.update.apply()
      entity.deletePicture()
      Success(true)
    }
  }


  def getChildren(entity: Category)(implicit session: DBSession = autoSession): List[Category] = {
    //    val cond = LikeConditionEscapeUtil.contains("/" +entity.id + "/")
    //    Category.findAllBy(sqls"pathtorootids LIKE ${cond}")
    Category.findAllBy(sqls"parent = ${entity.id}")
  }

  def childrenIds(entity: Category)(implicit session: DBSession = autoSession): List[Long] = {
    val cond = LikeConditionEscapeUtil.contains("/" + entity.id + "/")
    Category.findAllBy(sqls"pathtorootids LIKE ${cond}").map(c => c.id)
  }

  def getTexts(entity: Category)(implicit session: DBSession = autoSession): List[CategoryText] = {
    CategoryText.findAllBy(sqls"catid = ${entity.id}")
  }

  def getTextByLang(entity: Category, languageid: Long)(implicit session: DBSession = autoSession): Option[CategoryText] = {
    CategoryText.find(languageid, entity.id)
  }

  def getTextByDefaultLang(entity: Category)(implicit session: DBSession = autoSession): Option[CategoryText] = {
    for {
      lan <- Language.findBy(sqls"defaultlng = TRUE")
      text <- CategoryText.find(lan.id, entity.id)
    } yield text
  }

  def addGood(category: Category, good: Good)(implicit session: DBSession = Category.autoSession): Boolean = {
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

  def removeGood(category: Category, good: Good)(implicit session: DBSession = Category.autoSession): Boolean = {
    implicit val session = AutoSession
    def remove(): Boolean = {
      sql"""DELETE FROM good_category WHERE goodid = ${good.id} AND categoryid = ${category.id}"""
        .update.apply()
      true
    }
    Repository.contains("good_category", s"goodid=${good.id} AND categoryid=${category.id}") match {
      case Success(x) if x => remove()
      case Failure(t) => throw t
      case _ => true
    }

  }

  def hasAdvert(category: Category)(implicit session: DBSession = Category.autoSession): Boolean = {
    if (category.extension.nonEmpty) true else false
  }

  def goods(category: Category, orderby: String, offset: Int = 0, limit: Int = 10, withChildren: Boolean = false)(implicit session: DBSession = Category.autoSession): List[Good] = {
    DB readOnly { implicit session =>
      if (withChildren) {
        val ids = category.id :: childrenIds(category)
        sql"select * from good_category JOIN goods on goods.id=good_category.goodid WHERE categoryid IN (${ids}) ORDER BY ${orderby} LIMIT ${limit} OFFSET ${offset}"
          .map(rs => Good(rs)).list().apply()
      }
      else
        sql"select * from good_category JOIN goods on goods.id=good_category.goodid WHERE categoryid=${category.id} ORDER BY ${orderby} LIMIT ${limit} OFFSET ${offset}"
          .map(rs => Good(rs)).list().apply()
    }
  }


  private def pathtorootids(parent: Option[Long]): String = {
    if (parent.isDefined) {
      val parentObj = find(parent.get).get
      parentObj.pathtorootids + parentObj.id + "/"
    }
    else {
      "/"
    }
  }

  def roots: List[Category] = {
    Category.findAllBy( sqls"""pathtorootids = '/' """)
  }

  def pictureUrl(baseUrl: String, entity: Category): String = {
    entity.extension.map(e => baseUrl + entity.pictureDir + "/pictures/" + entity.id + "." + e)
      .getOrElse(baseUrl + entity.pictureDir + "/pictures/" + entity.id + "." + "jpg")
  }

  def pictureCategoryUrl(baseUrl: String, entity: Category): String = {
    baseUrl + "thumbs/" + entity.pictureDir + "/" + entity.id + "." + "jpg"
  }

  def pictureExtension(category: Category): String = {
    category.extension.getOrElse("jpg")
  }

}

class categoryParentEqualToCategory extends IllegalArgumentException

class categoryParentEqualToItsChildren extends IllegalArgumentException
