package dao

import javax.inject.{Inject, Singleton}

import anorm._
import models._
import play.api.Logger
import play.api.db.DBApi

@Singleton
class GameDao @Inject()(dBApi: DBApi) {

  private val gameParser: RowParser[Game] = (
    SqlParser.long("gid") ~
    SqlParser.str("player1Id") ~
    SqlParser.get[Option[String]]("player2Id") ~
    SqlParser.str("status") ~
    SqlParser.get[Option[String]]("turn") ~
    SqlParser.get[Option[String]]("winnerId")
  ).map {
    case gid ~ player1Id ~ player2Id ~ status ~ turn ~ winnerId =>
      Game(gid, player1Id, player2Id, GameStatus.withName(status), turn, winnerId)
  }

  def getWaitingGame: Option[Game] = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
            SELECT * FROM Game
            WHERE status = {status}
          """
        ).on("status" -> GameStatus.waiting_for_opponent.toString).as(gameParser.singleOpt)
      }
    } catch {
      case e: Exception => {
        Logger.error("An unexpected error occurred while getting a game from Game table.", e)
        throw e
      }
    }
  }

  def insertWaitingGame(pid: String): Long = {
    try {
      val gid: Option[Long] = dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
            INSERT INTO Game(player1Id, status)
            VALUES({player1Id}, {status})
          """
        ).on("player1Id" -> pid, "status" -> GameStatus.waiting_for_opponent.toString).executeInsert()
      }
      gid.get
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while inserting a game for player: ${pid}", e)
        throw e
      }
    }
  }

  def startGame(pid: String, gameId: Long, turn: Option[String]): Unit = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
          UPDATE Game SET player2Id = {player2Id}, status = {status}, turn = {turn}
          WHERE gid = {gameId}
        """
        ).on("player2Id" -> pid, "status" -> GameStatus.ongoing.toString, "gameId" -> gameId, "turn" -> turn).executeUpdate()
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while starting a game for game: ${gameId} and " +
          s"for player: ${pid}", e)
        throw e
      }
    }
  }

  def getGame(gid: Long): Option[Game] = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
          SELECT * FROM Game
          WHERE gid = {gid}
        """
        ).on("gid" -> gid).as(gameParser.singleOpt)
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while getting a game which is: ${gid}", e)
        throw e
      }
    }
  }

  def changeTurn(gid: Long): Unit = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
          UPDATE Game
          SET turn = IF(turn = player1Id, player2Id, player1Id)
          WHERE gid = {gid}
        """
        ).on("gid" -> gid).executeUpdate()
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while changing turn for game: ${gid}", e)
        throw e
      }
    }
  }

  def finishGame(move: Move): Unit = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
          UPDATE Game SET winnerId = {winnerId}, status = {status}
          WHERE gid = {gid}
        """
        ).on("winnerId" -> move.pid, "status" -> GameStatus.finished.toString, "gid" -> move.gid).executeUpdate()
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while finishing a game which is ${move.gid}", e)
        throw e
      }
    }
  }

  def getFinishedGame(gid: Long): Option[Game] = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
          SELECT * FROM Game
          WHERE gid = {gid} AND status = {status}

        """
        ).on("gid" -> gid, "status" -> GameStatus.finished.toString).as(gameParser.singleOpt)
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while getting a finished game which is: ${gid}", e)
        throw e
      }
    }
  }

}
