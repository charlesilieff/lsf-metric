package fr.rebaze.adapters

import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.domain.ports.models.RulesProgressByUserId
import fr.rebaze.models.{Interaction, UserFirstnameAndLastname, UserWithRules, Session as SessionModel}
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.{Task, ZIO, ZLayer}

import java.sql.Timestamp
import java.time.LocalDate

case class SessionRow(
  guid: String,
  actorGuid: String,
  levelGuid: String,
  interaction: JsonValue[Interaction]
)

case class UserWithRuleIdRow(
  actorGuid: String,
  ruleId: String
)

case class AccountRow(
  email: String,
  name: String,
  surname: String
)

case class UserAndInteractionRow(
  actorGuid: String,
  interaction: JsonValue[Interaction]
)
val MILLI_SECONDS_IN_DAY = 86400000

object SessionRepositoryLive:
  val layer: ZLayer[Quill.Postgres[CamelCase], Nothing, SessionRepositoryLive] =
    ZLayer.fromFunction(SessionRepositoryLive(_))
final case class SessionRepositoryLive(quill: Quill.Postgres[CamelCase]) extends SessionRepository:
  import quill.*

  inline private def querySession                                                        = quote(
    querySchema[SessionRow](entity = "SessionInteractionsWithAutoincrementId", _.actorGuid -> "actorGuid", _.levelGuid -> "levelGuid"))
  def sessionTimeStampRow(millisecondsTimestamp: Long): Quoted[Query[UserWithRuleIdRow]] = quote {
    sql"""SELECT actorguid,interaction FROM sessioninteractionswithautoincrementid WHERE ((interaction->>'timestamp')::bigint > ${lift(
        millisecondsTimestamp)} AND (interaction->>'timestamp')::bigint < ${lift(millisecondsTimestamp + MILLI_SECONDS_IN_DAY)})"""
      .as[Query[UserWithRuleIdRow]]
  }

  def progressAndActorGuidRow(actorGuid: String): Quoted[Query[UserAndInteractionRow]] = quote {
    sql"""SELECT DISTINCT actorguid, interaction FROM sessioninteractionswithautoincrementid WHERE actorguid = ${lift(
        actorGuid)} AND (interaction->>'progress')::float > 0"""
      .as[Query[UserAndInteractionRow]]
  }
  override def getAllSessionsByActorGuid(actorGuid: String): Task[Seq[SessionModel]]   =
    run(querySession.filter(_.actorGuid == lift(actorGuid)))
      .tap(x => ZIO.logDebug(s"Found ${x.length}")).map(values =>
        values
          .map(session => new SessionModel(session.guid, actorGuid = session.actorGuid, session.levelGuid, session.interaction.value)))

  override def getUsersWithRulesTrainedByDay(day: LocalDate): Task[Iterable[UserWithRules]] =
    val timestamp                = Timestamp.valueOf(day.atStartOfDay())
    val timestampsInMilliSeconds = timestamp.getTime

    run(sessionTimeStampRow(timestampsInMilliSeconds))
      .tap(x => ZIO.logInfo(s"Found ${x.length} users")).map(_.groupBy(_.actorGuid).map(session =>
        new UserWithRules(session._1, session._2.map(_.ruleId))))

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
          interactions.foldLeft[Seq[(String, Double)]](List.empty) {
            case (acc, (ruleId, progress)) =>
              acc.find(_._1 == ruleId) match {
                case Some((_, maxProgress)) => if (progress > maxProgress) (ruleId, progress) +: acc.filterNot(_._1 == ruleId) else acc
                case None                   => (ruleId, progress) +: acc
              }
          }))
    maxProgressByRuleId
      .map((userId, interactions) => RulesProgressByUserId(userId, interactions.toMap))

  override def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): Task[Seq[String]] = ???
