package controllers

import javax.inject.{Inject, Singleton}

import dao.GameService
import models._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import utils.Utils

import scala.util.{Failure, Success, Try}

@Singleton
class GameController @Inject() (gameService: GameService, util: Utils) extends Controller {

  def joinGame: Action[AnyContent] = Action { request: Request[AnyContent] =>
    try {
      request.body.asJson.map { json =>
        val board: Board = json.as[Board]
        val game: Game = gameService.joinGame(board)
        game.player2Id match {
          case None => {
            val board: Board = gameService.getPlayerBoard(game.gid, game.player1Id)
            val boardJson: JsValue = Json.toJson(board)
            val returnJson = Json.obj(
              "turn" -> gameService.getTurnString(game.turn, game.player1Id),
              "status" -> game.status,
              "board" -> boardJson
            )
            Ok(returnJson)
          }
          case Some(player2Id: String) => {
            val board: Board = gameService.getPlayerBoard(game.gid, player2Id)
            val boardJson: JsValue = Json.toJson(board)
            val returnJson = Json.obj(
              "gid" -> game.gid,
              "turn" -> gameService.getTurnString(game.turn, player2Id),
              "status" -> game.status,
              "board" -> boardJson
            )
            Ok(returnJson)
          }
        }
      }.getOrElse {
        BadRequest("Could not found json content!")
      }
    } catch {
      case e: Exception => {
        Logger.error("An unexpected error occurred while joining a game!", e)
        InternalServerError("An unexpected error occurred!")
      }
    }

  }

  def makeMove(gid: Long) = Action { request: Request[AnyContent] =>
    try {
      request.body.asJson.map { json =>
        val move = json.as[Move]
        val moveResultTry: Try[MoveResponse] = gameService.makeMove(move)
        moveResultTry match {
          case Success(moveResponse: MoveResponse) => {
            Ok(Json.toJson(moveResponse))
          }
          case Failure(exception) => {
            BadRequest(exception.getMessage)
          }
        }
      }.getOrElse {
        BadRequest("Could not found json content!")
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An error occurred while making a move for game: ${gid}", e)
        InternalServerError("An unexpected error occurred!")
      }
    }
  }

  def checkStatus(gid: Long, pid: String) = Action {
    try {
      gameService.getGameStatus(gid) match {
        case None => BadRequest("Game not found!")
        case Some(game: Game) => {
          val board: Board = gameService.getPlayerBoard(gid, pid)
          val boardJson: JsValue = Json.toJson(board)
          val returnJson = Json.obj(
            "turn" -> gameService.getTurnString(game.turn, pid),
            "status" -> game.status,
            "board" -> boardJson
          )
          Ok(returnJson)
        }
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while checking status for game: ${gid}" +
          s" and for player: ${pid}", e)
        InternalServerError("An unexpected error occurred!")
      }
    }
  }

  def viewFinishedGame(gid: Long) = Action {
    try {
      val finishedGame: Option[Game] = gameService.getFinishedGame(gid)
      finishedGame match {
        case None => BadRequest("Game not found or finished")
        case Some(game: Game) => {
          val boards: Seq[Board] = gameService.getPlayersBoards(gid)

          val boardJson = Json.toJson(boards)
          val returnJson = Json.obj(
            "winnerId" -> game.winnerId,
            "boards" -> boardJson
          )
          Ok(returnJson)
        }
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while viewing finished game : ${gid}", e)
        InternalServerError("An unexpected error occurred!")
      }
    }
  }

}
