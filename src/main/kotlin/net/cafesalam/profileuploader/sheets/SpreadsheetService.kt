package net.cafesalam.profileuploader.sheets

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream

class SpreadsheetService {

  private fun getCredentialsFromServiceAccount(authenticationData: String): GoogleCredentials {
    val inputStream = ByteArrayInputStream(authenticationData.toByteArray())
    val credentials = GoogleCredentials.fromStream(inputStream).createScoped(SCOPES)
    credentials.refreshIfExpired()
    return credentials
  }

  fun getSheetsClient(authenticationData: String): Sheets {
    val jsonFactory = GsonFactory.getDefaultInstance()
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    return Sheets.Builder(
      httpTransport,
      jsonFactory,
      HttpCredentialsAdapter(getCredentialsFromServiceAccount(authenticationData))
    )
      .setApplicationName(APPLICATION_NAME)
      .build()
  }

  companion object {
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)
    private const val APPLICATION_NAME = "Gradle Profiler Info Uploader"
  }
}