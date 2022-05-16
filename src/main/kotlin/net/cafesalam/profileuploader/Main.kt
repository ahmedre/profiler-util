package net.cafesalam.profileuploader

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import net.cafesalam.profileuploader.sheets.SpreadsheetService
import net.cafesalam.profileuploader.task.CheckDeltaUtil
import net.cafesalam.profileuploader.task.GradleProfilerDataUploader
import okio.ByteString.Companion.decodeBase64

class BenchmarkUtil : NoOpCliktCommand()

class CheckDeltas : CliktCommand() {
  private val width by option(help = "number of results to consider before and after each commit").int().default(5)
  private val threshold by option(help = "threshold that marks a build a delta in build times").int().default(25)
  private val authenticationData: String by mutuallyExclusiveOptions(
    option("--auth-file").file().convert { it.readText() },
    option("--auth-string").convert { it.decodeBase64()?.utf8() ?: "" }
  ).required()

  override fun run() {
    val spreadsheetService = SpreadsheetService()
    val sheetsService = spreadsheetService.getSheetsClient(authenticationData)
    CheckDeltaUtil.checkForNotableDelta(sheetsService, width, threshold)
  }
}

class UploadProfilingData : CliktCommand() {
  private val gitHash by option(help = "the git hash of this commit").required()
  private val benchmarkFile by option(help = "the gradle-profiler result csv file").file(mustExist = true).required()
  private val authenticationData: String by mutuallyExclusiveOptions(
    option("--auth-file").file().convert { it.readText() },
    option("--auth-string").convert { it.decodeBase64()?.utf8() ?: "" }
  ).required()

  override fun run() {
    val spreadsheetService = SpreadsheetService()
    val sheetsService = spreadsheetService.getSheetsClient(authenticationData)
    GradleProfilerDataUploader.parseAndWriteBenchmarks(sheetsService, benchmarkFile, gitHash)
  }
}

fun main(args: Array<String>) {
  BenchmarkUtil().subcommands(CheckDeltas(), UploadProfilingData()).main(args)
}