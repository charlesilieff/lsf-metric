package fr.rebaze.api.routes

import zio.json.{DeriveJsonCodec, JsonCodec}

case class ErrorInfo(
    error: String
)

object ErrorInfo:
  given JsonCodec[ErrorInfo] = DeriveJsonCodec.gen[ErrorInfo]
