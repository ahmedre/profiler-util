import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
}

dependencies {
  implementation("com.squareup.okio:okio:3.1.0")

  // force update commons-codec to work around CVE in 1.11 (transitive dep to google-api-client).
  implementation("commons-codec:commons-codec:1.15")
  implementation("com.google.api-client:google-api-client:1.34.0")
  implementation("com.google.oauth-client:google-oauth-client-jetty:1.33.2")
  implementation("com.google.apis:google-api-services-sheets:v4-rev20220411-1.32.1")
  implementation("com.google.auth:google-auth-library-oauth2-http:1.6.0")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}