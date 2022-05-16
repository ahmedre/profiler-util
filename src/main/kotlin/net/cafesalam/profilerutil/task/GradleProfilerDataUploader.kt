package net.cafesalam.profilerutil.task

import com.google.api.services.sheets.v4.Sheets
import net.cafesalam.profilerutil.benchmark.BenchmarkParser
import net.cafesalam.profilerutil.sheets.SpreadsheetUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

object GradleProfilerDataUploader {

  fun parseAndWriteBenchmarks(sheetsService: Sheets, file: File, gitHash: String, spreadsheetId: String, range: String) {
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