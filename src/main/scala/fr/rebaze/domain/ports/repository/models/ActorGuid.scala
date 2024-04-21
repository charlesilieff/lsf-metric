package fr.rebaze.domain.ports.repository.models

import io.getquill.MappedEncoding
import sttp.tapir.codec.zio.prelude.newtype.TapirNewtypeSupport
import zio.json.{JsonCodec, JsonFieldDecoder, JsonFieldEncoder}
import zio.prelude.Newtype

object ActorGuid extends Newtype[String] with TapirNewtypeSupport[String]:
  given codec: JsonCodec[Type]                          = derive
  implicit val jsonFiledEncoder: JsonFieldEncoder[Type] = (in: ActorGuid) => in.toString
  implicit val jsonFiledDecoder: JsonFieldDecoder[Type] = JsonFieldDecoder[String].map(ActorGuid.apply)

  given MappedEncoding[ActorGuid, String](_.toString)

  given MappedEncoding[String, ActorGuid](ActorGuid.apply)
type ActorGuid = ActorGuid.Type
