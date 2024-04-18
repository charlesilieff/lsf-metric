package fr.rebaze.domain.ports.repository.models

import fr.rebaze.domain.ports.repository.LevelProgressRepo
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

case class UserLevelsProgressAndRulesAnswers(actorGuid: String, levelProgress: Iterable[LevelProgressRepo])

object UserLevelsProgressAndRulesAnswers:
  given interactionZioEncoder: zio.json.JsonEncoder[UserLevelsProgressAndRulesAnswers] =
    DeriveJsonEncoder.gen[UserLevelsProgressAndRulesAnswers]
  given interactionZioDecoder: zio.json.JsonDecoder[UserLevelsProgressAndRulesAnswers] =
    DeriveJsonDecoder.gen[UserLevelsProgressAndRulesAnswers]
