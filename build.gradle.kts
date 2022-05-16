import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

configurations {
  create("r8")
}

plugins {
  application
  kotlin("jvm") version "1.6.21"
}

group = "net.cafesalam"
version = "1.0-SNAPSHOT"

application {
  mainClass.set("net.cafesalam.profileuploader.MainKt")
}

repositories {
  mavenCentral()
  google()
}

dependencies {
  "r8"("com.android.tools:r8:3.3.28")
  implementation("com.squareup.okio:okio:3.1.0")
  implementation("com.github.ajalt.clikt:clikt:3.4.2")

  // force update commons-codec to work around CVE in 1.11 (transitive dep to google-api-client).
  implementation("commons-codec:commons-codec:1.15")
  implementation("com.google.api-client:google-api-client:1.34.0")
  implementation("com.google.oauth-client:google-oauth-client-jetty:1.33.2")
  implementation("com.google.apis:google-api-services-sheets:v4-rev20220411-1.32.1")
  implementation("com.google.auth:google-auth-library-oauth2-http:1.6.0")
  testImplementation(kotlin("test"))
}

val fatJar = task("fatJar", type = Jar::class) {
  dependsOn(project.configurations["runtimeClasspath"])
  dependsOn(tasks.named("jar"))

  archiveClassifier.set("fat")
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE

  manifest {
    attributes["Main-Class"] = "net.cafesalam.profileuploader.MainKt"
  }

  val sourceClasses = sourceSets.main.get().output.classesDirs
  inputs.files(sourceClasses)

  doFirst {
    from(sourceClasses)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
  }
}

val r8File = File("$buildDir/libs/profileuploader-r8.jar")
val r8Jar = task("r8Jar", type = JavaExec::class) {
  dependsOn(project.configurations["runtimeClasspath"])
  inputs.files(fatJar.outputs.files)
  outputs.file(r8File)

  classpath = project.configurations["r8"]
  mainClass.set("com.android.tools.r8.R8")
  val arguments = mutableListOf(
    "--release",
    "--classfile",
    "--output", r8File.toString(),
    "--pg-conf", "src/main/rules.txt",
    "--lib", System.getProperty("java.home").toString()
  )

  doFirst {
    arguments.add(fatJar.archiveFile.get().asFile.absolutePath)
    args = arguments
  }
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}