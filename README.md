```
                  ____ __
   ___  _______  / _(_) /__ ____
  / _ \/ __/ _ \/ _/ / / -_) __/
 / .__/_/  \___/_//_/_/\__/_/
/_/                   utilities
```

# Profiler Utils

## Intro
Today, there are [many][1] [great][2] [articles][3] about optimizing Android builds. Larger companies are able to do neat things like [this][4]. For smaller companies, indie projects, or hobby projects, this is a bit trickier to do, often letting developers make changes and hope for the best.

This repository aims to contain a set of tools to make it easy for any project to monitor build times. In the future, this project may expand to support other types of benchmarks (i.e. macrobenchmark and microbenchmark tests). Today, the tools in this repository support writing data to a Google Sheet, though more options may be considered in the future.

## Setup
I am planning to write a detailed blog post about this, combined with better documentation. For now, here is some barebones documentation.

Obtain a Google Sheets service json file from the Credentials section [here][5]. Copy the email address associated with this account. Make a new spreadsheet on Google Drive, and grant the aforementioned email address edit permissions to the spreadsheet. Note that the spreadsheet id is the part between `/d/` and `/edit` in the url to the spreadsheet.

Don't forget to also enable Google Sheets API access from the same dashboard under "Enabled APIs and Services."

## Tools

### Gradle Profiler Data Uploader
This mode reads a `benchmark.csv` file from a [gradle-profiler][6] run and adds data for it to the Google Sheet.

Usage:

```sh
java -jar profileuploader-r8.jar upload-profiling-data \
   --spreadsheet-id <spreadsheet-id> \
   --auth-file <service_account_credentials.json> \
   --git-hash <git hash of the build> \
   --benchmark-file <path to benchmark.csv>
```

Note that, alternatively, you can use `--auth-string` with a base64 encoded json string of the contents of the crednetials file as well.

You can also optionally pass in a `--spreadsheet-write-range` flag (defaulting to `Sheet1!A1`), if your sheet is not called "Sheet1", and if the column doesn't start at `A1`.

### Delta Checker

Given a Google Sheet of benchmarking data, this runs a step algorithm as described [here][7] to detect whether a build likely caused an improvement or a regression.

Usage:

```sh
java -jar profileuploader-r8.jar check-deltas \
   --spreadsheet-id <spreadsheet-id> \
   --auth-file <service_account_credentials.json>
```

It optionally takes in the following parameters:

```
--width - the number of builds before and after the build to check.
--threshold - the threshold for which to consider something as a change.
```

Like the above, you can also pass in `--auth-string` with a base64 encoded json authentication file. You can also pass in a `--spreadsheet-read-range` to specify where to read the data (defaults to `Sheet1!A1:Z` - change Z if you have more than 26 scenarios, and change Sheet1 if your sheet with the data is not Sheet1).


[1]: https://developer.android.com/studio/build/optimize-your-build
[2]: https://www.zacsweers.dev/optimizing-your-kotlin-build/
[3]: https://proandroiddev.com/how-we-reduced-our-gradle-build-times-by-over-80-51f2b6d6b05b
[4]: https://developer.squareup.com/blog/measure-measure-measure/
[5]: https://console.cloud.google.com/apis/dashboard
[6]: https://github.com/gradle/gradle-profiler
[7]: https://medium.com/androiddevelopers/fighting-regressions-with-benchmarks-in-ci-6ea9a14b5c71
