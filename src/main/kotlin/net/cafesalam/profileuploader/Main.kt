package net.cafesalam.profileuploader

import com.google.api.services.sheets.v4.Sheets


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

  fun writeSheet(sheetsService: Sheets) {
    // test spreadsheet
    val spreadsheetId = "1DDdAZ_FFw9QMlRqfwnxoYY-XqJglnHWz9oG1Qjo2A2c"
    val range = "Sheet1!A1:D"
    val data: List<List<Any>> = listOf(
      listOf("2022-05-15 6:54", "scenario1", 1000, "abcdef"),
      listOf("2022-05-15 6:54", "scenario2", 1200, "abcdef")
    )
    val response = SpreadsheetUtil.writeRangeToSheet(sheetsService, spreadsheetId, range, data)
    println("appended: ${response.updates.updatedCells} cells")
  }
}

fun main() {
  val sheetsService = SpreadsheetService().getSheetsClient()
  val main = Main()
  main.readSheet(sheetsService)
  main.writeSheet(sheetsService)
}