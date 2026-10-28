import java.time.Duration

plugins {
    kotlin("jvm") version "2.4.21"
    application
}
repositories { mavenCentral() }
kotlin { jvmToolchain(25) }
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
application { mainClass.set("com.opensurvey.surveys.MainKt") }
tasks.test {
    useJUnitPlatform()
    timeout.set(Duration.ofSeconds(60))
    maxHeapSize = "256m"
    systemProperty("junit.jupiter.execution.timeout.default", "3 s")
    systemProperty("junit.jupiter.execution.timeout.thread.mode.default", "SEPARATE_THREAD")
}
