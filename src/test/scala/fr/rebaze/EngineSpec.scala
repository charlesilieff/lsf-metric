package fr.rebaze

import fr.rebaze.domain.ports.engine.{Engine, EngineLive}
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

import java.time.LocalDateTime
import scala.collection.immutable.SortedMap

def buildRuleMapInOrder(list: List[Boolean]): SortedMap[Long, Boolean] =
  SortedMap.from(list.zipWithIndex.map { case (b, i) => (1000 + i).toLong -> b })

object EngineSpec extends ZIOSpecDefault:
  val now  = LocalDateTime.now()
  def spec = suite("Engine learned rule")(
    test("only one error,rule is not learned") {
      // given

      val oneNotCorrect = buildRuleMapInOrder(false :: Nil)
      val learned       = Engine.isRuleLearned(oneNotCorrect)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learn, one correct") {
      // given

      val oneCorrect = buildRuleMapInOrder(true :: Nil)
      val learned    = Engine.isRuleLearned(oneCorrect)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learn, two correct") {
      // given

      val twoCorrectRules = buildRuleMapInOrder(true :: true :: Nil)

      val learned = Engine.isRuleLearned(twoCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("learn one rule, three correct") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(true :: true :: true :: Nil)
      val learned           = Engine.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("learn one rule, four correct") {
      // given

      val fourCorrectRules = buildRuleMapInOrder(true :: true :: true :: true :: Nil)
      val learned          = Engine.isRuleLearned(fourCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("rule learn, four correct, one not") {
      // given

      val fourCorrectOneNotCorrectRules = buildRuleMapInOrder(true :: true :: true :: true :: false :: Nil)
      val learned                       = Engine.isRuleLearned(fourCorrectOneNotCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("rule not learn, two correct, one not") {
      // given

      val twoCorrectOneNotCorrectRules = buildRuleMapInOrder(true :: true :: false :: Nil)
      val learned                      = Engine.isRuleLearned(twoCorrectOneNotCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learn, two correct, one not,one correct") {
      // given

      val rules = buildRuleMapInOrder(true :: true :: false :: true :: Nil)

      val learned = Engine.isRuleLearned(rules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("learn one rule despite one error") {
      // given

      val threeCorrectONotCorrectRules = buildRuleMapInOrder(true :: true :: true :: false :: Nil)
      val learned                      = Engine.isRuleLearned(threeCorrectONotCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("learn one rule despite two errors") {
      // given

      val rules = buildRuleMapInOrder(true :: false :: false :: true :: true :: true :: Nil)

      val learned = Engine.isRuleLearned(rules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("learn one rule despite multiple errors") {
      // given

      val rules = buildRuleMapInOrder(true :: false :: false :: true :: true :: false :: true :: true :: true :: true :: Nil)

      val learned = Engine.isRuleLearned(rules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("not learn one rule despite multiple errors") {
      // given

      val rules = buildRuleMapInOrder(true :: false :: false :: true :: true :: false :: true :: true :: true :: Nil)

      val learned = Engine.isRuleLearned(rules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learned with a lot of errors") {
      // given

      val rules = buildRuleMapInOrder(
        true :: false :: false :: true :: true :: false :: false :: true :: true :: true :: true :: false :: false :: true :: true :: true :: true :: Nil)

      val learned = Engine.isRuleLearned(rules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("learn one rule despite two errors") {
      // given

      val rules = buildRuleMapInOrder(
        true :: false :: false :: true :: true :: false :: false :: true :: true :: true :: true :: false :: false :: true :: true :: true :: true :: true :: true :: Nil)

      val learned = Engine.isRuleLearned(rules)
      // then
      assertZIO(learned)(isTrue)
    }
  ).provide(EngineLive.layer)
//{"ruleId": "5548443a-34eb-4fd3-80e2-fd3356da4289", "correct": true, "progress": null, "timestamp": 1672914713065, "exerciseId": "cebba45f-af9a-4c8c-b411-bf5aa08a5fd1"}
