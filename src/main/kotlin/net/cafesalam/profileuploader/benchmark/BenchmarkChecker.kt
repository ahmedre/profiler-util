package net.cafesalam.profileuploader.benchmark

import net.cafesalam.profileuploader.BenchmarkStep
import net.cafesalam.profileuploader.util.StepFit
import kotlin.math.absoluteValue
import kotlin.math.min

object BenchmarkChecker {

  fun checkResults(before: List<List<Any>>, after: List<List<Any>>, threshold: Int): List<BenchmarkStep> {
    // each row has <date> <git hash> [scenario_1_time, scenario_2_time ... scenario_n_time
    val runs = min(before.minOf { it.size }, after.minOf { it.size })
    return (1..runs).map { scenarioNumber ->
      // index 0 is the date, index 1 is the hash, so scenario 1 is index 2
      val scenarioIndex = scenarioNumber + 1
      val runsBefore = before.map { it[scenarioIndex] as Double }
      val runsAfter = after.map { it[scenarioIndex] as Double }

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