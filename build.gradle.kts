plugins {
    id("org.kodein.root")
}

allprojects {
    group = "org.kodein.memory"
    version = "0.7.0"

    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
}

val kotlinxSerializationVer by extra { "1.0.1" }

