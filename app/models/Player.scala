package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}

case class Player(pid: String, board: Board)

object Player {

  implicit val playerReads: Reads[Player] = (
    (JsPath \ "pid").read[String] and
    (JsPath \ "board").read[Board]
    )(Player.apply _)

  implicit val playerWrites: Writes[Player] = (
    (JsPath \ "pid").write[String] and
      (JsPath \ "board").write[Board]
    )(unlift(Player.unapply))

}