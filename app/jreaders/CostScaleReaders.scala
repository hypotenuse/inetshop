package jreaders

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import validators.{JCostScaleNew, JCostScaleUpdate}

object CostScaleReaders {


  implicit val costScaleNewReads: Reads[JCostScaleNew] =
    (
      (JsPath \ "start").read[Int](min(0) keepAnd max(999999)) and
      (JsPath \ "finish").read[Int](min(0) keepAnd max(999999)) and
      (JsPath \ "percent").read[BigDecimal](min[BigDecimal](0.0) keepAnd max(BigDecimal(999.0)))
    )(JCostScaleNew.apply _).filter(
      ValidationError("START_SHOULD_BE_SMALLER_THEN_FINISH")
    ) { obj =>
      obj.start < obj.finish
    }


  implicit val costScaleUpdateReads: Reads[JCostScaleUpdate] = (
        (JsPath \ "start").read[Int](min(0) keepAnd max(999999)) and
      (JsPath \ "finish").read[Int](min(0)  keepAnd max(999999)) and
      (JsPath \ "percent").read[BigDecimal](min[BigDecimal](0.0) keepAnd max[BigDecimal](999.0))
    )(JCostScaleUpdate.apply _).filter(
    ValidationError("START_SHOULD_BE_SMALLER_THEN_FINISH")
  ) { obj =>
    obj.start < obj.finish
  }
}
