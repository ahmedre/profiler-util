package net.cafesalam.profilerutil.benchmark

import net.cafesalam.profilerutil.util.StepFit
import kotlin.math.absoluteValue
import kotlin.math.min

object BenchmarkChecker {

  fun checkResults(before: List<List<Any>>, after: List<List<Any>>, threshold: Int): List<BenchmarkStep> {
    // each row has <date> <git hash> <notes> [scenario_1_time, scenario_2_time ... scenario_n_time
    val runs = min(before.minOf { it.size }, after.minOf { it.size }) - 3
    return (1..runs).map { scenarioNumber ->
      // index 0 is the date, index 1 is the hash, index 2 is notes, so scenario 1 is index 3
      val scenarioIndex = scenarioNumber + 2
      val runsBefore = before.map { it[scenarioIndex].toString().replace(",", "").toDouble() }
      val runsAfter = after.map { it[scenarioIndex].toString().replace(",", "").toDouble() }

      val stepDelta = StepFit.stepFit(runsBefore, runsAfter)
      if (stepDelta.absoluteValue >= threshold) {
        if (threshold > 0) BenchmarkStep.IMPROVEMENT
        else BenchmarkStep.REGRESSION
      } else {
        BenchmarkStep.NO_CHANGE
      }
    }
  }
}