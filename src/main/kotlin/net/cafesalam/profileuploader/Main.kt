package net.cafesalam.profileuploader

import com.google.api.services.sheets.v4.Sheets
import net.cafesalam.profileuploader.benchmark.BenchmarkChecker
import net.cafesalam.profileuploader.benchmark.BenchmarkParser
import net.cafesalam.profileuploader.sheets.SpreadsheetService
import net.cafesalam.profileuploader.sheets.SpreadsheetUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone


class Main {
  fun checkForNotableDelta(sheetsService: Sheets, width: Int, threshold: Int) {
    val spreadsheetId = Constants.spreadsheetId
    val range = Constants.spreadsheetDataRange

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

  fun parseAndWriteBenchmarks(sheetsService: Sheets, file: String, gitHash: String) {
    val spreadsheetId = Constants.spreadsheetId
    val range = Constants.spreadsheetWriteRange

    val benchmarkParser = BenchmarkParser()
    val results = benchmarkParser.parseBenchmarkResults(file)
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm").apply { timeZone = TimeZone.getTimeZone("GMT") }
    val date = dateFormatter.format(Date())

    val data: List<List<Any>> = listOf(
      listOf(date) + listOf(gitHash) + results.map { it.runs.average() }
    )
    val response = SpreadsheetUtil.writeRangeToSheet(sheetsService, spreadsheetId, range, data)
    println("appended: ${response.updates.updatedCells} cells")
  }
}

fun main(args: Array<String>) {
  val sheetsService = SpreadsheetService().getSheetsClient()
  if (args.size == 2) {
    val firstParam = args.first()
    val secondParam = args.last()

    val main = Main()
    val width = firstParam.toIntOrNull()
    val threshold = secondParam.toIntOrNull()
    if (width != null && threshold != null) {
      main.checkForNotableDelta(sheetsService, width, threshold)
    } else {
      main.parseAndWriteBenchmarks(sheetsService, args.first(), args.last())
    }
  } else {
    println("usage: ./gradlew run --args \"<path> <gitHash>\"")
    println(" OR")
    println("usage: ./gradlew run --args \"<width> <threshold>\"")
  }
}