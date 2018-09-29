package models

import models.GameStatus.GameStatus

case class Game(gid: Long, player1Id: String, player2Id: Option[String], status: GameStatus, var turn: Option[String] = None, winnerId: Option[String] =  None)

object GameStatus extends Enumeration {
  type GameStatus = Value
  val waiting_for_opponent, ongoing, finished = Value
}

