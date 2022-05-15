package net.cafesalam.profileuploader.benchmark

data class Benchmark(
  val scenario: String,
  val task: String,
  val value: String,
  val warmups: List<Int>,
  val runs: List<Int>
)