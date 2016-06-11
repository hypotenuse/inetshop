package controllers.admin

import models.{SupplierCurrencyRate, Supplier}
import play.api.libs.json._
import play.api.mvc.Action
import repositories.SupplierEditAggregate
import validators.{JSupplierNew, JSupplierUpdate}

class Suppliers extends BaseSecuredController{


  def add = admin(
    admin =>
      Action(parse.json) {implicit request =>
        import jreaders.SupplierReaders.supplierNewReads
        val json = request.body
        json.validate[JSupplierNew].fold(
          valid = { jSupplierNew =>
              val supplier=Supplier.create(jSupplierNew)
              Ok(Json.toJson(JsNumber(supplier.id)))
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )
      }
  )

  def update(id: Long) = admin(
    admin =>
      Action(parse.json) {implicit request =>
        import jreaders.SupplierReaders.supplierUpdateReads
        val json = request.body
        json.validate[JSupplierUpdate].fold(
          valid = { jSupplier =>
            try {
              Supplier.save(id, jSupplier)
              Ok(Json.toJson(JsString("Saved")))
            }
            catch {
              case e: NoSuchElementException => NotFound
            }
          },
          invalid = {
            errors => BadRequest(JsError.toJson(errors))
          }
        )

      }
  )

  def list = admin(
    admin =>
      Action { implicit request =>
        import jwriters.SupplierWriters._
        val result = Json.obj("data" -> Supplier.findAll())
        Ok(Json.toJson(result))
      }
  )

  def view(id: Long) = admin(
    admin =>
      Action { implicit request =>
        import jwriters.SupplierWriters.supplierEditWrites
        SupplierEditAggregate.get(id).map{
          n => Ok(Json.toJson(n))
        }.getOrElse(NotFound)
      }
  )

  def delete(id: Long) = admin(
    admin =>
      Action {
        Supplier.find(id).map{
          supplier =>
            supplier.destroy()
            Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )

  def deleteRate(id: Long, currencyId: Long) = admin(
    admin =>
      Action {
        SupplierCurrencyRate.find(supplier = id, currency = currencyId).map{
          currencyRate =>
            currencyRate.destroy()
            Ok(Json.toJson("SUCCESS"))
        }.getOrElse(NotFound)
      }
  )


}
