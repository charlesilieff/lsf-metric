package fr.rebaze

import fr.rebaze.adapters.EngineLive
import fr.rebaze.domain.services.engine.Engine
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

import java.time.LocalDateTime
import scala.collection.immutable.SortedMap

val now = LocalDateTime.now()

def buildRuleMapInOrder(correctCount: Int, errorCount: Int): SortedMap[LocalDateTime, Boolean] =
  val correct = List.fill(correctCount)(true)
  val error   = List.fill(errorCount)(false)
  val all     = correct ++ error
  SortedMap.from(all.zipWithIndex.map { case (b, i) => now.plusSeconds(i) -> b })

object EngineSpec extends ZIOSpecDefault:
  def spec = suite("Engine learned rule")(
    test("only one error,rule is not learned") {
      // given

      val oneNotCorrect = buildRuleMapInOrder(0, 1)
      val learned       = Engine.isRuleLearned(oneNotCorrect)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learn, one correct") {
      // given

      val oneCorrect = buildRuleMapInOrder(1, 0)
      val learned    = Engine.isRuleLearned(oneCorrect)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learn, two correct") {
      // given

      val twoCorrectRules = buildRuleMapInOrder(2, 0)

      val learned = Engine.isRuleLearned(twoCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("learn one rule, three correct") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(3, 0)
      val learned           = Engine.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("learn one rule, four correct") {
      // given

      val fourCorrectRules = buildRuleMapInOrder(4, 0)
      val learned          = Engine.isRuleLearned(fourCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("rule learn, four correct, one not") {
      // given

      val fourCorrectOneNotCorrectRules = buildRuleMapInOrder(4, 1)
      val learned                       = Engine.isRuleLearned(fourCorrectOneNotCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("rule not learn, two correct, one not") {
      // given

      val twoCorrectOneNotCorrectRules = buildRuleMapInOrder(2, 1)
      val learned                      = Engine.isRuleLearned(twoCorrectOneNotCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learn, two correct, one not,one correct") {
      // given

      val rules = buildRuleMapInOrder(2, 1) ++ Map(now.plusSeconds(3) -> true)

      val learned = Engine.isRuleLearned(rules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("learn one rule despite one error") {
      // given

      val threeCorrectONotCorrectRules = buildRuleMapInOrder(3, 1)
      val learned                      = Engine.isRuleLearned(threeCorrectONotCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("learn one rule despite two errors") {
      // given

      val oneCorrectTwoNotCorrectRules = buildRuleMapInOrder(1, 2)
      val threeCorrectRules            = buildRuleMapInOrder(3, 0)
      val learned                      = Engine.isRuleLearned(oneCorrectTwoNotCorrectRules ++ threeCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("learn one rule despite multiple errors") {
      // given

      val oneCorrectTwoNotCorrectRules = buildRuleMapInOrder(1, 2)
      val twoCorrectOneNotCorrectRules = buildRuleMapInOrder(2, 1)
      val fourCorrectRules             = buildRuleMapInOrder(4, 0)
      val learned                      = Engine.isRuleLearned(oneCorrectTwoNotCorrectRules ++ twoCorrectOneNotCorrectRules ++ fourCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("not learn one rule despite multiple errors") {
      // given

      val oneCorrectTwoNotCorrectRules = buildRuleMapInOrder(1, 2)
      val twoCorrectOneNotCorrectRules = buildRuleMapInOrder(2, 1)
      val threeCorrectRules            = buildRuleMapInOrder(3, 0)
      val learned                      = Engine.isRuleLearned(oneCorrectTwoNotCorrectRules ++ twoCorrectOneNotCorrectRules ++ threeCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learned with a lot of errors") {
      // given

      val oneCorrectTwoNotCorrectRules  = buildRuleMapInOrder(1, 2)
      val twoCorrectTwoNotCorrectRules  = buildRuleMapInOrder(2, 2)
      val fourCorrectTwoNOtCorrectRules = buildRuleMapInOrder(4, 2)
      val fiveCorrectRules              = buildRuleMapInOrder(5, 0)
      val learned                       = Engine.isRuleLearned(
        oneCorrectTwoNotCorrectRules ++ twoCorrectTwoNotCorrectRules ++ fourCorrectTwoNOtCorrectRules ++ fiveCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("learn one rule despite two errors") {
      // given

      val oneCorrectTwoNotCorrectRules  = buildRuleMapInOrder(1, 2)
      val twoCorrectTwoNotCorrectRules  = buildRuleMapInOrder(2, 2)
      val fourCorrectTwoNOtCorrectRules = buildRuleMapInOrder(4, 2)
      val sixCorrectRules               = buildRuleMapInOrder(6, 0)
      val learned                       = Engine.isRuleLearned(
        oneCorrectTwoNotCorrectRules ++ twoCorrectTwoNotCorrectRules ++ fourCorrectTwoNOtCorrectRules ++ sixCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    }
  ).provide(EngineLive.layer)
//{"ruleId": "5548443a-34eb-4fd3-80e2-fd3356da4289", "correct": true, "progress": null, "timestamp": 1672914713065, "exerciseId": "cebba45f-af9a-4c8c-b411-bf5aa08a5fd1"}
