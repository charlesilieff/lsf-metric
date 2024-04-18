package fr.rebaze.domain.ports.repository.models

import sttp.tapir.codec.zio.prelude.newtype.TapirNewtypeSupport
import zio.json.{JsonCodec, JsonFieldDecoder, JsonFieldEncoder}
import zio.prelude.Newtype

object RuleId extends Newtype[String] with TapirNewtypeSupport[String]:
  given codec: JsonCodec[Type]                          = derive
  implicit val jsonFiledEncoder: JsonFieldEncoder[Type] = (in: RuleId) => in.toString
  implicit val jsonFiledDecoder: JsonFieldDecoder[Type] = JsonFieldDecoder[String].map(RuleId.apply)
type RuleId = RuleId.Type
