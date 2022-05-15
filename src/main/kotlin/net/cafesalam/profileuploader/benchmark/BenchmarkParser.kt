package net.cafesalam.profileuploader.benchmark

import okio.buffer
import okio.source
import java.io.File

class BenchmarkParser {
  fun parseBenchmarkResults(file: String): List<Benchmark> {
    val items = parseBenchmarkFile(file)
    val scenarios = items["scenario"] ?: emptyList()
    val tasks = items["tasks"] ?: emptyList()
    val valueType = items["value"] ?: emptyList()

    val warmups = items.filterKeys { it.startsWith("warm-up build #") }
    val iterations = items.filterKeys { it.startsWith("measured build #") }

    return scenarios.mapIndexed { index, scenarioName ->
      val scenarioWarmups = warmups.values.map { warmup -> warmup[index].toIntOrNull() ?: 0 }
      val scenarioIterations = iterations.values.map { iteration -> iteration[index].toIntOrNull() ?: 0 }
      Benchmark(scenarioName, tasks[index], valueType[index], scenarioWarmups, scenarioIterations)
    }
  }

  private fun parseBenchmarkFile(file: String): Map<String, List<String>> {
    val map = mutableMapOf<String, List<String>>()
    File(file).inputStream().source().use { source ->
      source.buffer().use { bufferedSource ->
        while (true) {
          val line = bufferedSource.readUtf8Line() ?: break
          val pieces = line.split(",")
          map[pieces[0]] = pieces.subList(fromIndex = 1, toIndex = pieces.size)
        }
      }
    }
    return map
  }
}