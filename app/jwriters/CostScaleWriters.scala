package jwriters

import models.CostScale
import play.api.libs.json.{Writes, _}


object CostScaleWriters {
  implicit val costScaleWrites = new Writes[CostScale] {
    def writes(costScale: CostScale) = Json.obj(
      "id" -> costScale.id,
      "start" -> costScale.start,
      "finish" -> costScale.finish,
      "percent" -> costScale.percent
    )
  }
}
