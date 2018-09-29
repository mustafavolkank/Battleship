package models

import models.MoveResult.MoveResult
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class MoveResponse (result: MoveResult, shipType: Option[String])

object MoveResponse {

  implicit val locationWrites: Writes[MoveResponse] = (
    (JsPath \ "result").write[MoveResult] and
    (JsPath \ "shipType").writeNullable[String]
    )(unlift(MoveResponse.unapply))

}
