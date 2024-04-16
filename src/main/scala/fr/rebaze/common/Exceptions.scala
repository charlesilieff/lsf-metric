package fr.rebaze.common

object Exceptions {
  case class NotFound(message: String) extends RuntimeException(message)
}
