package models

import play.api.libs.json.{JsPath, Reads, Writes}
import play.api.libs.functional.syntax._


case class Ship(shipType: String, locations: Seq[Location])

object Ship {

  implicit val shipReads: Reads[Ship] = (
    (JsPath \ "shipType").read[String] and
    (JsPath \ "locations").read[Seq[Location]]
    )(Ship.apply _)

  implicit val shipWrites: Writes[Ship] = (
    (JsPath \ "shipType").write[String] and
    (JsPath \ "locations").write[Seq[Location]]
    )(unlift(Ship.unapply))
}
