buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
        maven(url = "https://dl.bintray.com/kodein-framework/kodein-dev")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:5.5.0")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "Kodein-Memory"

include(
        "kodein-memory",
        "kodein-memory-files",
        ""
)
