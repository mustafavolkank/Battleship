package dao
import javax.inject.{Inject, Singleton}

import anorm._
import models._
import play.api.Logger
import play.api.db.DBApi

@Singleton
class MoveDao @Inject()(dBApi: DBApi) {

  def insertMove(move: Move): Unit = {
    try {
      dBApi.database("default").withConnection { implicit connection =>
        SQL(
          """
          INSERT INTO Move(pid, locationX, locationY, gid, result)
          VALUES({pid}, {locationX}, {locationY}, {gid}, {result})
        """
        ).on("pid" -> move.pid, "locationX" -> move.locationX, "locationY" -> move.locationY, "gid" -> move.gid, "result" -> move.result.map(_.toString)).executeInsert()
      }
    } catch {
      case e: Exception => {
        Logger.error(s"An unexpected error occurred while inserting move for player ${move.pid} and " +
          s"for game: ${move.gid}", e)
        throw e
      }
    }
  }
}
