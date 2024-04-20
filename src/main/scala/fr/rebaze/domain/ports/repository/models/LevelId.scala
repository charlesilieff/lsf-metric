package fr.rebaze.domain.ports.repository.models

import io.getquill.MappedEncoding
import sttp.tapir.codec.zio.prelude.newtype.TapirNewtypeSupport
import zio.json.JsonCodec
import zio.prelude.Newtype

object LevelId extends Newtype[String] with TapirNewtypeSupport[String]:
  given JsonCodec[Type] = derive

  given MappedEncoding[LevelId, String](_.toString)
  given MappedEncoding[String, LevelId](LevelId.apply)
type LevelId = LevelId.Type
