package models

import models.MoveResult.MoveResult
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}


case class Move (pid: String, gid: Long, locationX: Int, locationY: Int, var result: Option[MoveResult] = None)

object MoveResult extends Enumeration {

  type MoveResult = Value
  val hit, sunk, missed = Value
  implicit val moveResultRead: Reads[MoveResult.Value] = Reads.enumNameReads(MoveResult)

}

object Move {

  implicit val moveReads: Reads[Move] = (
    (JsPath \ "pid").read[String] and
    (JsPath \ "gid").read[Long] and
    (JsPath \ "locationX").read[Int] and
    (JsPath \ "locationY").read[Int] and
    (JsPath \ "result").readNullable[MoveResult]
    )(Move.apply _)

  implicit val moveWrites: Writes[Move] = (
    (JsPath \ "pid").write[String] and
    (JsPath \ "gid").write[Long] and
    (JsPath \ "locationX").write[Int] and
    (JsPath \ "locationY").write[Int] and
    (JsPath \ "result").writeNullable[MoveResult.Value]
    )(unlift(Move.unapply))

}


