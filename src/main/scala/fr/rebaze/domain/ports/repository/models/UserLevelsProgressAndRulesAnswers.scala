package fr.rebaze.domain.ports.repository.models

case class UserLevelsProgressAndRulesAnswers(actorGuid: ActorGuid, levelProgress: Map[LevelId, LevelProgressAndRulesAnswerRepo])
