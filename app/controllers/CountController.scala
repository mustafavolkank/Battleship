package controllers

import javax.inject._

import anorm._
import models._
import play.api._
import play.api.db.DBApi
import play.api.db.DB
import play.api.mvc._
import services.Counter
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._ // Combinator syntax


/**
 * This controller demonstrates how to use dependency injection to
 * bind a component into a controller class. The class creates an
 * `Action` that shows an incrementing count to users. The [[Counter]]
 * object is injected by the Guice dependency injection system.
 */
@Singleton
class CountController @Inject() (counter: Counter, dBApi: DBApi) extends Controller {

  /**
    * Create an action that responds with the [[Counter]]'s current
    * count. The result is plain text. This `Action` is mapped to
    * `GET /count` requests by an entry in the `routes` config file.
    */
  def count = Action {
    Ok(counter.nextCount().toString)
  }

  /*def gameStartRequest = Action { request: Request[AnyContent] =>
    request.body.asJson.map { json =>
      val playerJson: JsObject = (json \ "Player").as[JsObject]
      val pid: String = (playerJson \ "pid").as[String]

      val boardJson: JsObject = (playerJson \ "board").as[JsObject]
      val shipsJson: JsArray = (boardJson \ "ships").as[JsArray]

      val ships: Seq[Ship] = shipsJson.value.map { shipJson: JsValue =>
        val shipType: String = (shipJson \ "shipType").as[String]
        val locationX: Int = (shipJson \ "locationX").as[Int]
        val locationY: Int = (shipJson \ "locationY").as[Int]
        new Ship(shipType, new Location(locationX, locationY))
      }

      val board = new Board(ships)
      val player = new Player(pid, board)

      val waitingGameId: Option[Int] = game.getWaitingGame
      waitingGameId match {
        case None => {
          val gid: Option[Long] = game.insertWaitingGame(player.pid)
          gid match {
            case None => {
              println("Could not insert waiting game!")
              InternalServerError("Error!")
            }
            case Some(gameId: Long) => {
              game.insertPlayerBoard(player, gameId)
            }
          }
        }
        case Some(gameId: Int) => {
          game.updateWaitingGame(player.pid, gameId)
          game.insertPlayerBoard(player, gameId)
        }
      }
      Ok("ok")
    }.getOrElse {
      BadRequest("Could not found json content!")
    }
  }*/
}
