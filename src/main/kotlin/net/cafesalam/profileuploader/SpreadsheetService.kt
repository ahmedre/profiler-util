package net.cafesalam.profileuploader

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileNotFoundException

class SpreadsheetService {

  private fun getCredentialsFromServiceAccount(): GoogleCredentials {
    val inputStream = Main::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
      ?: throw FileNotFoundException("could not find: $CREDENTIALS_FILE_PATH")
    val credentials = GoogleCredentials.fromStream(inputStream).createScoped(SCOPES)
    credentials.refreshIfExpired()
    return credentials
  }

  fun getSheetsClient(): Sheets {
    val jsonFactory = GsonFactory.getDefaultInstance()
    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    return Sheets.Builder(httpTransport, jsonFactory, HttpCredentialsAdapter(getCredentialsFromServiceAccount()))
      .setApplicationName(APPLICATION_NAME)
      .build()
  }

  companion object {
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)
    private const val CREDENTIALS_FILE_PATH = "/service_account.json"
    private const val APPLICATION_NAME = "Gradle Profiler Info Uploader"
  }
}