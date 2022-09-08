import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20-Beta"
    application
    id("com.google.cloud.tools.jib") version "3.3.0"
}

val javaVersion = 17

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "$javaVersion"
}

group = "se.rrva.gitlab.pipeline.notifier"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val commitSha: String = System.getenv("CI_COMMIT_SHA") ?: "test"

jib {
    from {
        image =
            "eclipse-temurin:17-jre"
    }
    to {
        image = "rrva/pipeline-notifier"
        tags = setOf(commitSha)
    }
    container {
        environment = mapOf(
            "TZ" to "Europe/Stockholm",
            "COMMIT_SHA" to commitSha
        )
        jvmFlags = listOf(
            "-Dlogback.configurationFile=logback.json.xml",
        )
        creationTime = "USE_CURRENT_TIMESTAMP"
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("ch.qos.logback:logback-classic:1.4.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.4")

    implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    implementation("ch.qos.logback.contrib:logback-jackson:0.1.5")

    implementation("org.eclipse.jetty.websocket:websocket-jetty-server:11.0.11")
    implementation("org.eclipse.jetty.websocket:websocket-jetty-api:11.0.11")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

tasks.test {
    useJUnitPlatform()
}



application {
    mainClass.set("se.rrva.gitlab.pipeline.notifier.MainKt")
}


tasks.register<GradleBuild>("buildAndPackage") {
    tasks = listOf("build", "jib")
}