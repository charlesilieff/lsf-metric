package fr.rebaze.domain.services.metrics.errors

object Exceptions:
  case class NotFound(message: String) extends RuntimeException(message)
