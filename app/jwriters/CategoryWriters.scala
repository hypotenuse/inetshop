package jwriters

import models.{Category, CategoryText}
import play.api.libs.json.{Writes, _}
import repositories.{CategorySelectAggregate, CategoryEdit}


object CategoryWriters {
  implicit val categoryWrites = new Writes[Category] {
    def writes(category: Category) = Json.obj(
      "id" -> category.id,
      "parent" -> category.parent,
      "pathtorootids" -> category.pathtorootids,
      "slug" -> category.slug,
      "onhome" -> category.onhome,
      "havePicture" -> category.picture.nonEmpty,
      "haveCategoryPicture" -> category.catpicture.nonEmpty
    )
  }

  implicit val categoryTextWrites = new Writes[CategoryText] {
    def writes(categoryText: CategoryText) = Json.obj(
      "catid" -> categoryText.catid,
      "languageid" -> categoryText.languageid,
      "title" -> categoryText.title,
      "description" -> categoryText.description,
      "metatitle" -> categoryText.metatitle,
      "metadescription" -> categoryText.metadescription,
      "pathtoroot" -> categoryText.pathtoroot
    )
  }

  implicit val categoryEditWrites = new Writes[CategoryEdit] {
    def writes(categoryEdit: CategoryEdit) = Json.obj(
      "category" -> Json.toJson(categoryEdit.category),
      "pictureUrl" -> Json.toJson(categoryEdit.pictureUrl),
      "pictureCategoryUrl" -> Json.toJson(categoryEdit.pictureCategoryUrl),
      "data" -> Json.toJson(
        categoryEdit.data.toMap
      )
    )
  }
  
  implicit val categorySelectAggregateWrites = new Writes[CategorySelectAggregate] {
    def writes(categorySelect: CategorySelectAggregate) = Json.obj(
      "id" -> JsNumber(categorySelect.id),
      "title" -> JsString(categorySelect.title),
      "pathtoroot" -> JsString(categorySelect.pathtoroot)
    )
  }
}
