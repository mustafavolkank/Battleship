package dao

import models._
import javax.inject.{Inject, Singleton}

import utils.Utils

import scala.util.{Failure, Success, Try}


@Singleton
class GameService @Inject() (gameDao: GameDao, boardDao: BoardDao, moveDao: MoveDao, util: Utils) {

  def joinGame(board: Board): Game = {
    val playerId: String = util.generateUuid
    val player: Player = Player(playerId, board)
    val waitingGame: Option[Game] = gameDao.getWaitingGame
    waitingGame match {
      case None => {
        val gid: Long = gameDao.insertWaitingGame(player.pid)
        boardDao.insertPlayerBoard(player, gid)
        gameDao.getGame(gid).get
      }
      case Some(game: Game) => {
        val turn: Int = util.randomTurn
        if (turn == 0) {
          game.turn = Some(game.player1Id)
        } else {
          game.turn = Some(player.pid)
        }
        gameDao.startGame(player.pid, game.gid, game.turn)
        boardDao.insertPlayerBoard(player, game.gid)
        gameDao.getGame(game.gid).get
      }
    }
  }

  private def takeShot(move: Move): MoveResponse = {
    val boards: List[Board] = boardDao.getAttackedBoard(move)
    var hitShipType: String = ""

    val board: Board = foldBoard(boards, move.pid)
    var doesMoveHit: Boolean = false
    board.ships.foreach { ship =>
      ship.locations.foreach { location =>
        if (move.locationX == location.x && move.locationY == location.y && location.isHit == false) {
          doesMoveHit = true
          hitShipType = ship.shipType
        }
      }
    }
    if (doesMoveHit) {
      val hitShip: Ship = board.ships.find(_.shipType == hitShipType).get
      val moveResponse: MoveResponse = if (isShipSunk(move, hitShip)) {
        move.result = Some(MoveResult.sunk)
        if (isGameFinished(board, hitShip)) {
          gameDao.finishGame(move)
        }
        MoveResponse(move.result.get, Some(hitShipType))
      } else {
        move.result = Some(MoveResult.hit)
        MoveResponse(move.result.get, None)
      }
      val id: Long = boardDao.getAttackedShipId(move, hitShipType)
      boardDao.makeShot(move, id)
      moveResponse
    } else {
      move.result = Some(MoveResult.missed)
      gameDao.changeTurn(move.gid)
      MoveResponse(move.result.get, None)
    }
  }

  private def isGameFinished(board: Board, ship: Ship): Boolean = {
    if (board.ships.filter {
      e => !(e.shipType == ship.shipType)
    }.forall(e => e.locations.forall(e => e.isHit == true))) {
      true
    } else {
      false
    }
  }

  private def isShipSunk(move: Move, hitShip: Ship): Boolean = {
    if (hitShip.locations.filter {
      e => !(e.x == move.locationX && e.y == move.locationY)
    }.forall(e => e.isHit == true)) {
      true
    } else {
      false
    }
  }

  def makeMove(move: Move): Try[MoveResponse] = {
   val game: Option[Game] = gameDao.getGame(move.gid)
    game match {
      case None => Failure(new RuntimeException("Game not found!"))
      case Some(game: Game) => {
        if (game.status == GameStatus.finished) {
          Failure(new RuntimeException("Game is already finished!"))
        }
        else {
          val moveResponse: MoveResponse = takeShot(move)
          moveDao.insertMove(move)
          Success(moveResponse)
        }
      }
    }
  }

  def getTurnString(turn: Option[String], pid: String): Option[String] = {
    turn.map { e =>
      if (e == pid) {
        "You"
      } else {
        "Opponent"
      }
    }
  }

  def getPlayerBoard(gid: Long, pid: String): Board = {
    val boards: List[Board] = boardDao.getBoard(gid, pid)
    foldBoard(boards,pid)
  }

  def getGameStatus(gid: Long): Option[Game] = {
    gameDao.getGame(gid)
  }

  def getFinishedGame(gid: Long): Option[Game] = {
    gameDao.getFinishedGame(gid)
  }

  def getPlayersBoards(gid: Long): List[Board] = {
    val boards: List[Board] = boardDao.getPlayersBoards(gid)
    val groupedBoards: Map[String, List[Board]] = boards.groupBy(_.pid)
    groupedBoards.map { case (pid: String, boards: List[Board]) =>
      foldBoard(boards, pid)
    }.toList
  }

  def foldBoard(boards: List[Board], pid: String): Board = {
    val foldedBoard: Board = boards.foldLeft(Board("", Seq.empty[Ship])) { (accumulatedBoard: Board, nextBoard: Board) =>
      Board(pid, accumulatedBoard.ships ++ nextBoard.ships)
    }
    val groupedShips: Map[String, Seq[Ship]] = foldedBoard.ships.groupBy(_.shipType)

    val foldedShips: Seq[Ship] = groupedShips.map { case (shipType: String, ships: Seq[Ship]) =>
      ships.foldLeft(Ship(shipType, Seq.empty[Location])) { (accumulatedShip: Ship, nextShip: Ship) =>
        Ship(shipType, accumulatedShip.locations ++ nextShip.locations)
      }
    }.toSeq

    Board(foldedBoard.pid, foldedShips, foldedBoard.gid)
  }
}
