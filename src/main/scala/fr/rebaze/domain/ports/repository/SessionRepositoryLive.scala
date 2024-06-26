package fr.rebaze.domain.ports.repository

import fr.rebaze.domain.ports.models.LevelsProgressByActorGuid
import fr.rebaze.domain.ports.repository.models.row.*
import fr.rebaze.domain.ports.repository.models.{
  ActorGuid,
  Interaction,
  LevelId,
  LevelProgressAndRulesAnswerRepo,
  UserLevelsProgressAndRulesAnswers,
  Session as SessionModel
}
import fr.rebaze.models.UserFirstnameAndLastname
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.{Task, ZIO, ZLayer}

import java.sql.Timestamp
import java.time.LocalDate
import scala.collection.immutable.SortedMap

val MILLI_SECONDS_IN_DAY = 86400000

case class ActorGuidRow(
  actorGuid: ActorGuid
)

object SessionRepositoryLive:
  val layer: ZLayer[Quill.Postgres[CamelCase], Nothing, SessionRepositoryLive] =
    ZLayer.fromFunction(SessionRepositoryLive(_))
final case class SessionRepositoryLive(quill: Quill.Postgres[CamelCase]) extends SessionRepository:
  import quill.*

  inline private def querySession = quote(
    querySchema[SessionRow](entity = "SessionInteractionsWithAutoincrementId", _.actorGuid -> "actorGuid", _.levelGuid -> "levelGuid"))
  private def allInteractionsForActorList(actorGuids: Iterable[ActorGuid]): Quoted[Query[ActorAndInteractionAndLevelGuidRow]] =
//    def innerQuery = quote((actorGuid: Query[ActorGuid]) =>
//      querySchema[ActorGuidRow](entity = "SessionInteractionsWithAutoincrementId", _.actorGuid -> "actorGuid").filter(p =>
//        actorGuid.contains(p.actorGuid)))
//    quote {
//      querySchema[ActorAndInteractionAndLevelGuidRow](
//        entity = "SessionInteractionsWithAutoincrementId",
//        _.actorGuid -> "actorGuid",
//        _.levelGuid -> "levelGuid").filter(p => innerQuery(liftQuery(actorGuids)).contains(p.actorGuid))
//    }
    quote {
      sql"""WITH actor_guids AS (SELECT unnest(ARRAY[${liftQuery(actorGuids)}]) AS actor_guid)
           SELECT actorguid, levelguid, interaction FROM sessioninteractionswithautoincrementid WHERE actorguid IN (SELECT actor_guid FROM actor_guids)"""
        .as[Query[ActorAndInteractionAndLevelGuidRow]]
    }

  private def actorRow(millisecondsTimestamp: Long): Quoted[Query[ActorGuidRow]] = quote {
    sql"""SELECT DISTINCT actorguid FROM sessioninteractionswithautoincrementid WHERE
             ((interaction->>'timestamp')::bigint > ${lift(millisecondsTimestamp)} AND (interaction->>'timestamp')::bigint < ${lift(
        millisecondsTimestamp + MILLI_SECONDS_IN_DAY)} AND actorguid LIKE '%@lsf')"""
      .as[Query[ActorGuidRow]]
//    sql"""SELECT DISTINCT actorguid FROM sessioninteractionswithautoincrementid WHERE actorguid LIKE '%@lsf'"""
//      .as[Query[ActorGuidRow]]
  }

  private def levelProgressAndActorGuidRow(actorGuid: ActorGuid): Quoted[Query[ActorAndInteractionRow]] = quote {
    sql"""SELECT DISTINCT actorguid,levelguid, interaction FROM sessioninteractionswithautoincrementid WHERE actorguid = ${lift(
        actorGuid)} AND (interaction->>'progress')::float > 0"""
      .as[Query[ActorAndInteractionRow]]
  }
  override def getAllSessionsByActorGuid(actorGuid: ActorGuid): Task[Iterable[SessionModel]]            =
    run(querySession.filter(_.actorGuid == lift(actorGuid)))
      .tap(x => ZIO.logDebug(s"Found ${x.length}")).map(values =>
        values
          .map(session => new SessionModel(session.guid, actorGuid = session.actorGuid, session.levelGuid, session.interaction.value)))

  private def updateLevelsProgress(
    acc: Map[LevelId, LevelProgressAndRulesAnswerRepo],
    userAndInteraction: ActorAndInteractionAndLevelGuidRow): Map[LevelId, LevelProgressAndRulesAnswerRepo] = {
    val newCompletionDate = acc
      .get(userAndInteraction.levelGuid).flatMap(_.completionDate).orElse(
        if (userAndInteraction.interaction.value.progress.contains(1.0)) Some(userAndInteraction.interaction.value.timestamp) else None)

    acc.get(userAndInteraction.levelGuid) match {
      case Some(LevelProgressAndRulesAnswerRepo(levelId, maxProgress, rulesAnswers, completionDate)) =>
        val newRulesAnswers = rulesAnswers.updatedWith(userAndInteraction.interaction.value.ruleId) {
          case Some(ruleAnswers) =>
            Some(ruleAnswers + (userAndInteraction.interaction.value.timestamp -> userAndInteraction.interaction.value.correct))
          case None              =>
            Some(SortedMap(userAndInteraction.interaction.value.timestamp -> userAndInteraction.interaction.value.correct))
        }
        if (userAndInteraction.interaction.value.progress.getOrElse(0d) >= maxProgress)
          acc.updated(
            levelId,
            LevelProgressAndRulesAnswerRepo(
              levelId,
              userAndInteraction.interaction.value.progress.getOrElse(0d),
              newRulesAnswers,
              newCompletionDate))
        else
          acc.updated(levelId, LevelProgressAndRulesAnswerRepo(levelId, maxProgress, newRulesAnswers, completionDate))
      case None                                                                                      =>
        acc.updated(
          userAndInteraction.levelGuid,
          LevelProgressAndRulesAnswerRepo(
            userAndInteraction.levelGuid,
            userAndInteraction.interaction.value.progress.getOrElse(0),
            Map(
              userAndInteraction.interaction.value.ruleId -> SortedMap(
                userAndInteraction.interaction.value.timestamp -> userAndInteraction.interaction.value.correct)),
            newCompletionDate
          )
        )
    }
  }

  override def getActorsLevelsProgressAndRulesAnswers(actorGuids: Iterable[ActorGuid]): Task[Iterable[UserLevelsProgressAndRulesAnswers]] =
    run(allInteractionsForActorList(actorGuids))
      .tap(x => ZIO.logInfo(s"Found ${x.length} actors interactions for ${actorGuids.size} actors")).map(
        _.groupBy(_.actorGuid).map(userAndLevelAndInteraction =>

          val levelsProgress = userAndLevelAndInteraction
            ._2.foldLeft[Map[LevelId, LevelProgressAndRulesAnswerRepo]](Map.empty)(updateLevelsProgress)

          UserLevelsProgressAndRulesAnswers(
            userAndLevelAndInteraction._1,
            levelsProgress
          )
        ))

  override def getUsersNameAndFirstName(actorGuid: ActorGuid): Task[UserFirstnameAndLastname] =
    // remove "@lsf" at the end of userId :
    val email = actorGuid.toString.substring(0, actorGuid.toString.length - 4)
    run(querySchema[AccountRow](entity = "dbaccount").filter(_.email == lift(email)).take(1))
      .map(value => value.head).map(value => UserFirstnameAndLastname(lastname = Some(value.surname), firstname = Some(value.name)))

  override def getRulesProgressByActorGuid(actorGuid: ActorGuid): Task[LevelsProgressByActorGuid] =
    val progress             = run(levelProgressAndActorGuidRow(actorGuid))
    val progressByUser       =
      progress
        .map(value =>
          value
            .groupBy(_.actorGuid).headOption.map((userId, interactions) =>
              (
                userId,
                interactions.map(interaction => (interaction.levelGuid, interaction.interaction.value.progress.getOrElse(0.0)))))).map(
          value => value.getOrElse((actorGuid, List.empty)))
    val maxProgressByLevelId = progressByUser
      .map((userId, interactions) =>
        (
          userId,
          interactions.foldLeft[Seq[(LevelId, Double)]](List.empty) {
            case (acc, (levelId, progress)) =>
              acc.find(_._1 == levelId) match {
                case Some((_, maxProgress)) => if (progress > maxProgress) (levelId, progress) +: acc.filterNot(_._1 == levelId) else acc
                case None                   => (levelId, progress) +: acc
              }
          }))
    maxProgressByLevelId
      .map((actorGuid, interactions) => LevelsProgressByActorGuid(actorGuid, interactions.toMap))

  override def getActorGuidsByDay(day: LocalDate): Task[Iterable[ActorGuid]] =
    val timestamp                = Timestamp.valueOf(day.atStartOfDay())
    val timestampsInMilliSeconds = timestamp.getTime
    run(actorRow(timestampsInMilliSeconds)).map(_.map(_.actorGuid)).tap(x => ZIO.logInfo(s"Found ${x.size} actors for ${day}"))
