package net.cafesalam.profilerutil.task

import com.google.api.services.sheets.v4.Sheets
import net.cafesalam.profilerutil.benchmark.BenchmarkChecker
import net.cafesalam.profilerutil.benchmark.BenchmarkStep
import net.cafesalam.profilerutil.sheets.SpreadsheetUtil

object CheckDeltaUtil {

  fun checkForNotableDelta(sheetsService: Sheets, width: Int, threshold: Int, spreadsheetId: String, range: String) {
    val responseValues = SpreadsheetUtil.readRangeFromSheet(sheetsService, spreadsheetId, range)
    if (responseValues.isEmpty()) {
      println("No data found")
    } else {
      responseValues.forEach { row ->
        println(row)
      }

      val numberOfRunsToConsider = 1 + (2 * width)
      if (responseValues.size >= numberOfRunsToConsider) {
        val runsToConsider = responseValues.subList(
          fromIndex = responseValues.size - numberOfRunsToConsider,
          toIndex = responseValues.size
        )

        val before = runsToConsider.take(width)
        val after = runsToConsider.takeLast(width)

        val scenariosRow = responseValues.first()
        val mergeToConsider = runsToConsider[width]

        val results = BenchmarkChecker.checkResults(before, after, threshold)
        results.forEachIndexed { index, scenarioResult ->
          // date, git hash
          val scenariosOffset = 2
          val gitHash = mergeToConsider[1]
          val scenario = scenariosRow[index + scenariosOffset]

          when (scenarioResult) {
            BenchmarkStep.REGRESSION -> {
              println("commit $gitHash showed a regression for $scenario.")
            }
            BenchmarkStep.IMPROVEMENT -> {
              println("commit $gitHash showed an improvement for $scenario.")
            }
            BenchmarkStep.NO_CHANGE -> {
              println("commit $gitHash showed no change for $scenario.")
            }
          }
        }
      } else {
        println("Not enough data to analyze")
      }
    }
  }
}