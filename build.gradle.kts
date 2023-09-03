import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

configurations {
  create("r8")
}

plugins {
  application
  kotlin("jvm") version "1.9.10"
}

group = "net.cafesalam"
version = "0.0.3"

application {
  mainClass.set("net.cafesalam.profilerutil.MainKt")
}

repositories {
  mavenCentral()
  google()
}

dependencies {
  "r8"("com.android.tools:r8:3.3.75")
  implementation("com.squareup.okio:okio:3.5.0")
  implementation("com.github.ajalt.clikt:clikt:3.5.0")

  implementation("com.google.apis:google-api-services-sheets:v4-rev20230815-2.0.0")
  implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
  testImplementation(kotlin("test"))
}

val fatJar = task("fatJar", type = Jar::class) {
  dependsOn(project.configurations["runtimeClasspath"])
  dependsOn(tasks.named("jar"))

  archiveClassifier.set("fat")
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE

  manifest {
    attributes["Main-Class"] = "net.cafesalam.profilerutil.MainKt"
  }

  val sourceClasses = sourceSets.main.get().output.classesDirs
  inputs.files(sourceClasses)

  doFirst {
    from(sourceClasses)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
  }
}

val r8File = File("$buildDir/libs/profiler-util-$version-r8.jar")
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
  kotlin.jvmToolchain(8)
}
