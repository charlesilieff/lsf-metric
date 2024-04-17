package fr.rebaze

import fr.rebaze.adapters.EngineLive
import fr.rebaze.domain.services.engine.EngineService
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

import java.time.LocalDateTime
import scala.collection.immutable.ListMap

val now = LocalDateTime.now()

def buildRuleMapInOrder(correctCount: Int, errorCount: Int): ListMap[LocalDateTime, Boolean] =
  val correct = List.fill(correctCount)(true)
  val error   = List.fill(errorCount)(false)
  val all     = correct ++ error
  ListMap.from(all.zipWithIndex.map { case (b, i) => now.plusSeconds(i) -> b })

object EngineSpec extends ZIOSpecDefault:
  def spec = suite("Engine learned rule")(
    test("only one error,rule is not learned") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(0, 1)
      val learned           = EngineService.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learn, one correct") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(1, 0)
      val learned           = EngineService.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learn, two correct") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(2, 0)
      println(threeCorrectRules)
      val learned           = EngineService.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("learn one rule, three correct") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(3, 0)
      val learned           = EngineService.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("learn one rule, four correct") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(4, 0)
      val learned           = EngineService.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("rule learn, four correct, one not") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(4, 1)
      val learned           = EngineService.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("rule not learn, two correct, one not") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(2, 1)
      val learned           = EngineService.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("rule not learn, two correct, one not,one correct") {
      // given

      val rules = buildRuleMapInOrder(2, 1) ++ Map(now.plusSeconds(3) -> true)

      val learned = EngineService.isRuleLearned(rules)
      // then
      assertZIO(learned)(isFalse)
    },
    test("learn one rule despite one error") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(3, 1)
      val learned           = EngineService.isRuleLearned(threeCorrectRules)
      // then
      assertZIO(learned)(isTrue)
    },
    test("learn one rule despite two errors") {
      // given

      val threeCorrectRules = buildRuleMapInOrder(1, 2)
      val rules             = buildRuleMapInOrder(3, 0)
      val learned           = EngineService.isRuleLearned(threeCorrectRules ++ rules)
      // then
      assertZIO(learned)(isTrue)
    }
  ).provide(EngineLive.layer)
//{"ruleId": "5548443a-34eb-4fd3-80e2-fd3356da4289", "correct": true, "progress": null, "timestamp": 1672914713065, "exerciseId": "cebba45f-af9a-4c8c-b411-bf5aa08a5fd1"}
