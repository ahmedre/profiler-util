package net.cafesalam.profilerutil.sheets

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import net.cafesalam.profilerutil.benchmark.Benchmark

object SpreadsheetUtil {

  fun readRangeFromSheet(sheetsService: Sheets, spreadsheetId: String, range: String): List<List<Any>> {
    val response = sheetsService.spreadsheets().values()
      .get(spreadsheetId, range)
      .execute()
    return response.getValues() ?: emptyList()
  }

  fun writeRangeToSheet(
    sheetsService: Sheets,
    spreadsheetId: String,
    range: String,
    date: String,
    gitHash: String,
    notes: String,
    results: List<Benchmark>
  ): AppendValuesResponse {
    val columns = readRangeFromSheet(sheetsService, spreadsheetId, range)
    val headers = columns.firstOrNull()?.map { it.toString() } ?: emptyList()

    val seen = mutableListOf<Benchmark>()
    val data = mutableListOf<Any>()

    headers.forEach { header ->
      when (header) {
        "Date" -> {
          data.add(date)
        }
        "Git Hash" -> {
          data.add(gitHash)
        }
        "Notes" -> {
          data.add(notes)
        }
        else -> {
          val result = results.firstOrNull { it.scenario == header }
          if (result != null) {
            seen.add(result)
            data.add(result.runs.average())
          } else {
            data.add("?")
          }
        }
      }
    }

    val remaining = results - seen.toSet()
    val headersToWrite = mutableListOf<Any>()
    if (remaining.isNotEmpty()) {
      if (headers.isEmpty()) {
        headersToWrite.add("Date")
        headersToWrite.add("Git Hash")
      } else {
        headers.forEach { headersToWrite.add(it) }
      }

      remaining.forEach {
        headersToWrite.add(it.scenario)
        data.add(it.runs.average())
      }

      val headerRowBody = ValueRange().setValues(listOf(headersToWrite))
      if (headers.isNotEmpty()) {
        println("headers, updating header rows to $range - $headerRowBody - headers btw: $headers")
        sheetsService.spreadsheets().values()
          .update(spreadsheetId, range, headerRowBody)
          .setValueInputOption("USER_ENTERED")
          .execute()
        headersToWrite.clear()
      } else {
        data.add(0, date)
        data.add(1, gitHash)
      }
    }

    val dataToUpload = if (headersToWrite.isEmpty()) listOf(data) else listOf(headersToWrite, data)
    val body = ValueRange().setValues(dataToUpload)
    return sheetsService.spreadsheets().values().append(spreadsheetId, range, body)
      .setValueInputOption("USER_ENTERED")
      .execute()
  }
}