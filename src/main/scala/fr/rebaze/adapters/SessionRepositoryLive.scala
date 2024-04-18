package fr.rebaze.adapters

import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.domain.ports.models.RulesProgressByUserId
import fr.rebaze.models.{Interaction, UserFirstnameAndLastname, UserWithRules, Session as SessionModel}
import io.getquill.*
import io.getquill.jdbczio.Quill
import sttp.tapir.codec.zio.prelude.newtype.TapirNewtypeSupport
import zio.json.*
import zio.prelude.Newtype
import zio.{Task, ZIO, ZLayer}

import java.sql.Timestamp
import java.time.LocalDate

case class SessionRow(
  guid: String,
  actorGuid: String,
  levelGuid: String,
  interaction: JsonValue[Interaction]
)

case class AccountRow(
  email: String,
  name: String,
  surname: String
)

object LevelId extends Newtype[String] with TapirNewtypeSupport[String]:
  implicit val codec: JsonCodec[Type] = derive
type LevelId = LevelId.Type

case class ActorAndInteractionAndLevelGuidRow(
  actorGuid: String,
  levelGuid: String,
  interaction: JsonValue[Interaction]
)

case class ActorAndInteractionRow(
  actorGuid: String,
  interaction: JsonValue[Interaction]
)

case class LevelProgress(levelId: LevelId, completionPercentage: Double)

object LevelProgress:
  given sessionZioEncoder: zio.json.JsonEncoder[LevelProgress] = DeriveJsonEncoder.gen[LevelProgress]

  given sessionZioDecoder: zio.json.JsonDecoder[LevelProgress] = DeriveJsonDecoder.gen[LevelProgress]
val MILLI_SECONDS_IN_DAY = 86400000

object SessionRepositoryLive:
  val layer: ZLayer[Quill.Postgres[CamelCase], Nothing, SessionRepositoryLive] =
    ZLayer.fromFunction(SessionRepositoryLive(_))
final case class SessionRepositoryLive(quill: Quill.Postgres[CamelCase]) extends SessionRepository:
  import quill.*

  inline private def querySession                                                                         = quote(
    querySchema[SessionRow](entity = "SessionInteractionsWithAutoincrementId", _.actorGuid -> "actorGuid", _.levelGuid -> "levelGuid"))
  def sessionTimeStampRow(millisecondsTimestamp: Long): Quoted[Query[ActorAndInteractionAndLevelGuidRow]] = quote {
    sql"""SELECT actorguid, levelguid, interaction FROM sessioninteractionswithautoincrementid WHERE ((interaction->>'timestamp')::bigint > ${lift(
        millisecondsTimestamp)} AND (interaction->>'timestamp')::bigint < ${lift(
        millisecondsTimestamp + MILLI_SECONDS_IN_DAY)} AND actorguid LIKE '%@lsf')"""
      .as[Query[ActorAndInteractionAndLevelGuidRow]]
  }

  def progressAndActorGuidRow(actorGuid: String): Quoted[Query[ActorAndInteractionRow]]   = quote {
    sql"""SELECT DISTINCT actorguid, interaction FROM sessioninteractionswithautoincrementid WHERE actorguid = ${lift(
        actorGuid)} AND (interaction->>'progress')::float > 0"""
      .as[Query[ActorAndInteractionRow]]
  }
  override def getAllSessionsByActorGuid(actorGuid: String): Task[Iterable[SessionModel]] =
    run(querySession.filter(_.actorGuid == lift(actorGuid)))
      .tap(x => ZIO.logDebug(s"Found ${x.length}")).map(values =>
        values
          .map(session => new SessionModel(session.guid, actorGuid = session.actorGuid, session.levelGuid, session.interaction.value)))

  override def getLsfUsersWithRulesTrainedByDay(day: LocalDate): Task[Iterable[UserWithRules]] =
    val timestamp                = Timestamp.valueOf(day.atStartOfDay())
    val timestampsInMilliSeconds = timestamp.getTime

    run(sessionTimeStampRow(timestampsInMilliSeconds))
      .tap(x => ZIO.logInfo(s"Found ${x.length} users for timestamp ${timestampsInMilliSeconds}")).map(
        _.groupBy(_.actorGuid).map(userAndLevelAndInteraction =>

          val levelsProgress = userAndLevelAndInteraction
            ._2.foldLeft[Seq[(LevelId, Double)]](List.empty) {
              case (acc, userAndInteraction) =>
                acc.find(_._1 == LevelId(userAndInteraction.levelGuid)) match {
                  case Some((levelId, maxProgress)) =>
                    if (userAndInteraction.interaction.value.progress.getOrElse(0d) > maxProgress)
                      (levelId, userAndInteraction.interaction.value.progress.getOrElse(0d)) +: acc.filterNot(_._1 == levelId)
                    else acc
                  case None                         => (LevelId(userAndInteraction.levelGuid), userAndInteraction.interaction.value.progress.getOrElse(0)) +: acc
                }
            }
          new UserWithRules(userAndLevelAndInteraction._1, levelsProgress.map((ruleId, progress) => LevelProgress(ruleId, progress)))
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
          interactions.foldLeft[Seq[(String, Double)]](List.empty) {
            case (acc, (ruleId, progress)) =>
              acc.find(_._1 == ruleId) match {
                case Some((_, maxProgress)) => if (progress > maxProgress) (ruleId, progress) +: acc.filterNot(_._1 == ruleId) else acc
                case None                   => (ruleId, progress) +: acc
              }
          }))
    maxProgressByRuleId
      .map((userId, interactions) => RulesProgressByUserId(userId, interactions.toMap))

  override def getLevelIdsByUserIdByDay(userId: String, day: LocalDate): Task[Iterable[String]] = ???
