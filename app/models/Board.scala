package models

import play.api.libs.functional.syntax._
import play.api.libs.json._


case class Board (pid: String, ships: Seq[Ship], gid: Option[Long] = None)

object Board {

  implicit val boardReads: Reads[Board] = (
    (JsPath \ "pid").read[String](" ") and
    (JsPath \ "ships").read[Seq[Ship]] and
    (JsPath \ "gid").readNullable[Long]
    )(Board.apply _)

  implicit val boardWrites: Writes[Board] = (
    (JsPath \ "pid").write[String] and
    (JsPath \ "ships").write[Seq[Ship]] and
    (JsPath \ "gid").writeNullable[Long]
    )(unlift(Board.unapply))
}
