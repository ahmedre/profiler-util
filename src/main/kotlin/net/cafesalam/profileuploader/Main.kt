package net.cafesalam.profileuploader

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.google.api.services.sheets.v4.Sheets
import net.cafesalam.profileuploader.benchmark.BenchmarkChecker
import net.cafesalam.profileuploader.benchmark.BenchmarkParser
import net.cafesalam.profileuploader.sheets.SpreadsheetService
import net.cafesalam.profileuploader.sheets.SpreadsheetUtil
import java.io.File
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

  fun parseAndWriteBenchmarks(sheetsService: Sheets, file: File, gitHash: String) {
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

class BenchmarkUtil : NoOpCliktCommand()

class CheckDeltas : CliktCommand() {
  private val width by option(help = "number of results to consider before and after each commit").int().default(5)
  private val threshold by option(help = "threshold that marks a build a delta in build times").int().default(25)

  override fun run() {
    val sheetsService = SpreadsheetService().getSheetsClient()
    Main().checkForNotableDelta(sheetsService, width, threshold)
  }
}

class UploadProfilingData : CliktCommand() {
  private val gitHash by option(help = "the git hash of this commit").required()
  private val benchmarkFile by option(help = "the gradle-profiler result csv file").file(mustExist = true).required()

  override fun run() {
    val sheetsService = SpreadsheetService().getSheetsClient()
    Main().parseAndWriteBenchmarks(sheetsService, benchmarkFile, gitHash)
  }
}

fun main(args: Array<String>) {
  BenchmarkUtil().subcommands(CheckDeltas(), UploadProfilingData()).main(args)
}