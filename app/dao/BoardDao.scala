package dao

import javax.inject.{Inject, Singleton}

import anorm._
import models._
import play.api.Logger
import play.api.db.DBApi

@Singleton
class BoardDao @Inject()(dBApi: DBApi) {

  private val boardParser: RowParser[Board] = (
    SqlParser.str("pid") ~
    SqlParser.str("shipType") ~
    SqlParser.int("locationX") ~
    SqlParser.int("locationY") ~
    SqlParser.long("gid") ~
    SqlParser.bool("isHit")
  ).map {
    case pid ~ shipType ~ locationX ~ locationY ~ gid ~ isHit =>
      val ship: Ship = Ship(shipType, Seq(Location(locationX, locationY, isHit)))
      Board(pid, Seq(ship), Some(gid))
  }


  def insertPlayerBoard(player: Player, gid: Long): Unit = {
    try {
      player.board.ships.foreach { ship =>
        val id: Option[Long] = dBApi.database("default").withConnection { implicit connection =>
          SQL(
            """
              INSERT INTO Board(gid, pid, shipType)
              VALUES({gameId}, {playerId}, {shipType})
            """
          ).on("gameId" -> gid, "playerId" -> player.pid, "shipType" -> ship.shipType).executeInsert()
        }
        ship.locations.foreach { location =>
          dBApi.database("default").withConnection { implicit connection =>
            SQL(
              """
                INSERT INTO Location(shipId, locationX, locationY)
                VALUES({id}, {locationX}, {locationY})
              """
            ).on("id" -> id.get, "locationX" -> location.x, "locationY" -> location.y).executeInsert()
          }
        }
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while inserting a board for player: ${player.pid} and" +
          s"for game: ${gid}", e)
        throw e
      }
    }
  }

  def getAttackedBoard(move: Move): List[Board] = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
            SELECT board.*, location.* FROM Board board INNER JOIN Location location
            ON board.id = location.shipId
            WHERE board.pid != {pid} AND board.gid = {gid}
          """
        ).on("pid" -> move.pid, "gid" -> move.gid).as(boardParser.*)
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while getting attacked board for game: ${move.gid}", e)
        throw e
      }
    }
  }

  def getAttackedShipId(move: Move, hitShipType: String): Long = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
            SELECT id FROM Board
            WHERE pid != {pid} AND gid = {gid} AND shipType = {hitShipType}
          """
        ).on("pid" -> move.pid, "gid" -> move.gid, "hitShipType" -> hitShipType).as(SqlParser.scalar[Long].single)
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while making shot for player: ${move.pid} and" +
          s"for game: ${move.gid}", e)
        throw e
      }
    }
  }

  def getBoard(gid: Long, pid: String): List[Board] = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
            SELECT board.*, location.* FROM Board board INNER JOIN Location location
            ON board.id = location.shipId
            WHERE gid = {gid} AND pid = {pid}
          """
        ).on("gid" -> gid, "pid" -> pid).as(boardParser.*)
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while getting board for player: ${pid} and" +
          s"for game: ${gid}", e)
        throw e
      }
    }
  }

  def getPlayersBoards(gid: Long): List[Board] = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
            SELECT board.*, location.* FROM Board board INNER JOIN Location location
            ON board.id = location.shipId
            WHERE gid = {gid}
          """
        ).on("gid" -> gid).as(boardParser.*)
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while getting both player's boards for game: ${gid}", e)
        throw e
      }
    }
  }

  def makeShot(move: Move, id: Long): Unit = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
            UPDATE Location SET isHit = 1
            WHERE shipId = {id} AND locationX = {locationX} AND locationY = {locationY}
          """
        ).on("id" -> id, "locationX" -> move.locationX, "locationY" -> move.locationY).executeUpdate()
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while shooting location for game: ${move.gid}", e)
        throw e
      }
    }
  }

}
