package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Location(x: Int, y: Int, isHit: Boolean = false)

object Location {

  implicit val locationReads: Reads[Location] = (
    (JsPath \ "locationX").read[Int] and
    (JsPath \ "locationY").read[Int] and
    (JsPath \ "isHit").read[Boolean](false)
    )(Location.apply _)

  implicit val locationWrites: Writes[Location] = (
    (JsPath \ "locationX").write[Int] and
    (JsPath \ "locationY").write[Int] and
    (JsPath \ "isHit").write[Boolean]
    )(unlift(Location.unapply))

}


