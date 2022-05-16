package net.cafesalam.profilerutil.sheets

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange

object SpreadsheetUtil {

  fun readRangeFromSheet(sheetsService: Sheets, spreadsheetId: String, range: String): List<List<Any>> {
    val response = sheetsService.spreadsheets().values()
      .get(spreadsheetId, range)
      .execute()
    return response.getValues()
  }

  fun writeRangeToSheet(
    sheetsService: Sheets,
    spreadsheetId: String,
    range: String,
    data: List<List<Any>>
  ): AppendValuesResponse {
    val body = ValueRange().setValues(data)
    return sheetsService.spreadsheets().values().append(spreadsheetId, range, body)
      .setValueInputOption("USER_ENTERED")
      .execute()
  }
}