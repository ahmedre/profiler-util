package net.cafesalam.profilerutil.benchmark

data class Benchmark(
  val scenario: String,
  val task: String,
  val value: String,
  val warmups: List<Float>,
  val runs: List<Float>
)