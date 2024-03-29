buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://raw.githubusercontent.com/kosi-libs/kodein-internal-gradle-plugin/mvn-repo")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:6.23.1")
    }
}

apply { plugin("org.kodein.settings") }

// Kotlin Low-level I/O
rootProject.name = "Klio"

include(
    "klio",
    "klio-files",
    "klio-crypto",
    ""
)
