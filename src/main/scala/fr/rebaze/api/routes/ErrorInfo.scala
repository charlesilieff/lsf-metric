package fr.rebaze.api.routes

import zio.json.{DeriveJsonCodec, JsonCodec}
case class ErrorInfo(
  error: String
)

object ErrorInfo {
  implicit val toto: JsonCodec[ErrorInfo] = DeriveJsonCodec.gen[ErrorInfo]
}
