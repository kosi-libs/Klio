buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://raw.githubusercontent.com/Kodein-Framework/kodein-internal-gradle-plugin/mvn-repo")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:6.15.4")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "Kodein-Memory"

include(
    "kodein-memory",
    "kodein-memory-files",
    "kodein-memory-crypto",
    ""
)
