package fr.rebaze.adapters

import fr.rebaze.domain.services.engine.EngineService
import zio.{Task, ULayer, ZIO, ZLayer}

import java.time.LocalDateTime

enum Sate:
  case NEVER
  case ERROR
  case ONE
  case TWO
  case THREE
  case FOUR
  case FIVE
  case LAST
val state = Array(Sate.NEVER, Sate.ERROR, Sate.ONE, Sate.TWO, Sate.THREE, Sate.FOUR, Sate.FIVE, Sate.LAST)

case class RuleState(timeError: Int, timeOk: Int, state: Sate)

object EngineLive:
  val layer: ULayer[EngineLive] =
    ZLayer.succeed(EngineLive())
final case class EngineLive() extends EngineService:
  def updateRuleState(acc: RuleState, entry: (LocalDateTime, Boolean)): RuleState = acc.state match {
    case Sate.LAST      => acc
    case _ if !entry._2 => RuleState(acc.timeError + 1, acc.timeOk, Sate.ERROR)
    case Sate.NEVER     => RuleState(acc.timeError, acc.timeOk + 1, Sate.FOUR)
    case Sate.ERROR     =>
      val boxIndex = Math.max(state.indexOf(Sate.ONE), state.indexOf(Sate.LAST) - acc.timeError)
      RuleState(acc.timeError, acc.timeOk + 1, state(boxIndex))
    case _              =>
      val boxIndex = Math.min(state.indexOf(acc.state) + 1, state.indexOf(Sate.LAST))
      RuleState(acc.timeError, acc.timeOk + 1, state(boxIndex))
  }

  override def isRuleLearned(ruleInteractions: Map[LocalDateTime, Boolean]): Task[Boolean] =
    println(s"ruleInteractions: $ruleInteractions")
    ruleInteractions.map(entry => println(s"entry: $entry"))
    ZIO.succeed(
      ruleInteractions
        .foldLeft(RuleState(0, 0, Sate.NEVER))(updateRuleState).state == Sate.LAST)
