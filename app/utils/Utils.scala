package utils

import javax.inject.Singleton

@Singleton
class Utils {
  def generateUuid: String = java.util.UUID.randomUUID.toString

  def randomTurn: Int = {
    val random = scala.util.Random
    random.nextInt(2)
  }

  /*def createLocationsJson(locations: Seq[Location]): Seq[JsObject] = {
    locations.map { location =>
      Json.obj(
        "locationX" -> location.x,
        "locationY" -> location.y,
        "isHit" -> location.isHit
      )
    }
  }

  def createShipsJson(ships: Seq[Ship]): Seq[JsObject] = {
    ships.map { ship =>
      Json.obj(
        "shipType" -> ship.shipType,
        "locations" -> createLocationsJson(ship.locations)
      )
    }
  }

  def createBoardsJson(boards: Seq[Board]): Seq[JsObject] = {
    boards.map { board =>
      Json.obj(
        "pid" -> board.pid,
        "ships" -> createShipsJson(board.ships)
      )
    }
  }*/

}
