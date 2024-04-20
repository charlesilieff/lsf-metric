package fr.rebaze.domain.ports.repository

import fr.rebaze.domain.ports.models.RulesProgressByUserId
import fr.rebaze.domain.ports.repository.models.row.*
import fr.rebaze.domain.ports.repository.models.{Interaction, LevelId, LevelProgressRepo, RuleId, UserLevelsProgressAndRulesAnswers, Session as SessionModel}
import fr.rebaze.models.UserFirstnameAndLastname
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.{Task, ZIO, ZLayer}

import java.sql.Timestamp
import java.time.LocalDate
import scala.collection.immutable.SortedMap

val MILLI_SECONDS_IN_DAY = 86400000

object SessionRepositoryLive:
  val layer: ZLayer[Quill.Postgres[CamelCase], Nothing, SessionRepositoryLive] =
    ZLayer.fromFunction(SessionRepositoryLive(_))
final case class SessionRepositoryLive(quill: Quill.Postgres[CamelCase]) extends SessionRepository:
  import quill.*

  inline private def querySession                                                                                 = quote(
    querySchema[SessionRow](entity = "SessionInteractionsWithAutoincrementId", _.actorGuid -> "actorGuid", _.levelGuid -> "levelGuid"))
  private def sessionTimeStampRow(millisecondsTimestamp: Long): Quoted[Query[ActorAndInteractionAndLevelGuidRow]] = quote {
    sql"""SELECT actorguid, levelguid, interaction FROM sessioninteractionswithautoincrementid WHERE  ((interaction->>'timestamp')::bigint > ${lift(
        millisecondsTimestamp)} AND (interaction->>'timestamp')::bigint < ${lift(
        millisecondsTimestamp + MILLI_SECONDS_IN_DAY)} AND actorguid LIKE '%@lsf'"""
      .as[Query[ActorAndInteractionAndLevelGuidRow]]
  }

  private def progressAndActorGuidRow(actorGuid: String): Quoted[Query[ActorAndInteractionRow]] = quote {
    sql"""SELECT DISTINCT actorguid, interaction FROM sessioninteractionswithautoincrementid WHERE actorguid = ${lift(
        actorGuid)} AND (interaction->>'progress')::float > 0"""
      .as[Query[ActorAndInteractionRow]]
  }
  override def getAllSessionsByActorGuid(actorGuid: String): Task[Iterable[SessionModel]]       =
    run(querySession.filter(_.actorGuid == lift(actorGuid)))
      .tap(x => ZIO.logDebug(s"Found ${x.length}")).map(values =>
        values
          .map(session => new SessionModel(session.guid, actorGuid = session.actorGuid, session.levelGuid, session.interaction.value)))

  override def getUsersLevelsProgressAndRulesAnswers(day: LocalDate): Task[Iterable[UserLevelsProgressAndRulesAnswers]] =
    val timestamp                = Timestamp.valueOf(day.atStartOfDay())
    val timestampsInMilliSeconds = timestamp.getTime

    run(sessionTimeStampRow(timestampsInMilliSeconds))
      .tap(x => ZIO.logInfo(s"Found ${x.length} users interaction for timestamp ${timestampsInMilliSeconds}")).map(
        _.groupBy(_.actorGuid).map(userAndLevelAndInteraction =>

          val levelsProgress = userAndLevelAndInteraction
            ._2.foldLeft[Seq[(LevelId, Double, Map[RuleId, SortedMap[Long, Boolean]])]](Seq.empty) {
              case (acc, userAndInteraction) =>
                acc.find(_._1 == userAndInteraction.levelGuid) match {
                  case Some((levelId, maxProgress, rulesAnswers)) =>
                    val newRulesAnswers = rulesAnswers.updatedWith(userAndInteraction.interaction.value.ruleId) {
                      case Some(ruleAnswers) =>
                        Some(ruleAnswers + (userAndInteraction
                          .interaction.value.timestamp -> userAndInteraction.interaction.value.correct))
                      case None              =>
                        Some(SortedMap(userAndInteraction.interaction.value.timestamp -> userAndInteraction.interaction.value.correct))
                    }
                    if (userAndInteraction.interaction.value.progress.getOrElse(0d) > maxProgress)
                      (levelId, userAndInteraction.interaction.value.progress.getOrElse(0d), newRulesAnswers) +: acc.filterNot(
                        _._1 == levelId)
                    else (levelId, maxProgress, newRulesAnswers) +: acc.filterNot(_._1 == levelId)
                  case None                                       =>
                    (
                      userAndInteraction.levelGuid,
                      userAndInteraction.interaction.value.progress.getOrElse(0),
                      Map(
                        userAndInteraction.interaction.value.ruleId ->
                          SortedMap(userAndInteraction.interaction.value.timestamp -> userAndInteraction.interaction.value.correct)
                      )) +: acc
                }
            }

          UserLevelsProgressAndRulesAnswers(
            userAndLevelAndInteraction._1,
            levelsProgress.map((ruleId, progress, rulesAnswers) => LevelProgressRepo(ruleId, progress, rulesAnswers)))
        ))

  override def getUsersNameAndFirstName(userId: String): Task[UserFirstnameAndLastname] =
    // remove "@lsf" at the end of userId :
    val email = userId.substring(0, userId.length - 4)
    run(querySchema[AccountRow](entity = "dbaccount").filter(_.email == lift(email)).take(1))
      .map(value => value.head).map(value => UserFirstnameAndLastname(lastname = Some(value.surname), firstname = Some(value.name)))

  override def getRulesProgressByUserId(userId: String): Task[RulesProgressByUserId] =
    val progress            = run(progressAndActorGuidRow(userId))
    val progressByUser      =
      progress
        .map(value =>
          value
            .groupBy(_.actorGuid).headOption.map((userId, interactions) =>
              (
                userId,
                interactions.map(interaction =>
                  (interaction.interaction.value.ruleId, interaction.interaction.value.progress.getOrElse(0.0)))))).map(value =>
          value.getOrElse((userId, List.empty)))
    val maxProgressByRuleId = progressByUser
      .map((userId, interactions) =>
        (
          userId,
          interactions.foldLeft[Seq[(RuleId, Double)]](List.empty) {
            case (acc, (ruleId, progress)) =>
              acc.find(_._1 == ruleId) match {
                case Some((_, maxProgress)) => if (progress > maxProgress) (ruleId, progress) +: acc.filterNot(_._1 == ruleId) else acc
                case None                   => (ruleId, progress) +: acc
              }
          }))
    maxProgressByRuleId
      .map((userId, interactions) => RulesProgressByUserId(userId, interactions.toMap))
