package net.cafesalam.profileuploader.util

import kotlin.math.pow
import kotlin.math.sqrt

// via https://medium.com/androiddevelopers/fighting-regressions-with-benchmarks-in-ci-6ea9a14b5c71
object StepFit {
  private fun List<Double>.sumSquaredError(): Double {
    return sumOf { (it - average()).pow(2.0) }
  }

  fun stepFit(before: List<Double>, after: List<Double>): Double {
    val totalSquaredError = before.sumSquaredError() + after.sumSquaredError()
    val stepError = sqrt(totalSquaredError) / (before.size + after.size)
    return if (stepError == 0.0) {
      0.0
    } else {
      (before.average() - after.average()) / stepError
    }
  }
}