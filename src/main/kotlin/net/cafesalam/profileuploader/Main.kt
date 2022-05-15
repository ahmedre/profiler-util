package net.cafesalam.profileuploader

import com.google.api.services.sheets.v4.Sheets
import net.cafesalam.profileuploader.benchmark.BenchmarkParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone


class Main {
  fun readSheet(sheetsService: Sheets) {
    // Google public test spreadsheet for reading
    val spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
    val range = "Class Data!A2:E"

    val responseValues = SpreadsheetUtil.readRangeFromSheet(sheetsService, spreadsheetId, range)
    if (responseValues.isEmpty()) {
      println("No data found")
    } else {
      println("Name, Major")
      responseValues.forEach { row ->
        println("${row[0]}, ${row[4]}")
      }
    }
  }

  fun parseAndWriteBenchmarks(sheetsService: Sheets, file: String, gitHash: String) {
    val range = "Sheet1!A1"
    val spreadsheetId = "1DDdAZ_FFw9QMlRqfwnxoYY-XqJglnHWz9oG1Qjo2A2c"

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
    val main = Main()
    main.parseAndWriteBenchmarks(sheetsService, args.first(), args.last())
  } else {
    println("usage: ./gradlew run --args \"<path> <gitHash>\"")
  }
}