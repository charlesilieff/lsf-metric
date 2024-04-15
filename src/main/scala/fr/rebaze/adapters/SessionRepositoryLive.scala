package fr.rebaze.adapters

import fr.rebaze.domain.ports.SessionRepository
import fr.rebaze.models.{Interaction, User, UserFirstnameAndLastname, Session as SessionModel}
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

case class UserRow(
  actorGuid: String
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

  inline private def querySession                                              = quote(
    querySchema[SessionRow](entity = "SessionInteractionsWithAutoincrementId", _.actorGuid -> "actorGuid", _.levelGuid -> "levelGuid"))
  def sessionTimeStampRow(millisecondsTimestamp: Long): Quoted[Query[UserRow]] = quote {
    sql"""SELECT DISTINCT actorguid FROM sessioninteractionswithautoincrementid WHERE ((interaction->>'timestamp')::bigint > ${lift(
        millisecondsTimestamp)} AND (interaction->>'timestamp')::bigint < ${lift(millisecondsTimestamp + MILLI_SECONDS_IN_DAY)})"""
      .as[Query[UserRow]]
  }

  def progressAndActorGuidRow(actorGuid: String): Quoted[Query[UserAndInteractionRow]] = quote {
    sql"""SELECT actorguid, interaction FROM sessioninteractionswithautoincrementid WHERE actorguid = ${lift(
        actorGuid)} AND (interaction->>'progress')::float > 0"""
      .as[Query[UserAndInteractionRow]]
  }
  override def getAllSessionsByActorGuid(actorGuid: String): Task[Seq[SessionModel]]   =
    run(querySession.filter(_.actorGuid == lift(actorGuid)))
      .tap(x => ZIO.logInfo(s"Found $x")).map(values =>
        values
          .map(session => new SessionModel(session.guid, actorGuid = session.actorGuid, session.levelGuid, session.interaction.value)))

  override def getUsersByDay(day: LocalDate): Task[Seq[User]] =
    val timestamp                = Timestamp.valueOf(day.atStartOfDay())
    val timestampsInMilliSeconds = timestamp.getTime
    println(s"Timestamp: $timestampsInMilliSeconds")
    run(sessionTimeStampRow(timestampsInMilliSeconds))
      .tap(x => ZIO.logInfo(s"Found ${x.length} users.")).map(value => value.map(session => new User(session.actorGuid)))

  override def getUsersNameAndFirstName(userId: String): Task[UserFirstnameAndLastname] =
    // remove "@lsf" at the end of userId :
    val email = userId.substring(0, userId.length - 4)
    run(querySchema[AccountRow](entity = "dbaccount").filter(_.email == lift(email)).take(1))
      .map(value => value.head).map(value => UserFirstnameAndLastname(lastname = Some(value.surname), firstname = Some(value.name)))

  override def getGlobalProgressByUserIdAndRuleIds(userId: String, ruleIds: Seq[String]): Task[Float] =
    val query = quote {
      querySchema[SessionRow](entity = "SessionInteractionsWithAutoincrementId", _.actorGuid -> "actorGuid")
        .filter(_.actorGuid == lift(userId))
        .filter(session => liftQuery(ruleIds).contains(session.levelGuid))
    }
    run(progressAndActorGuidRow(userId)).tap(value => ZIO.logInfo(value.toString())).as(0.23f)
