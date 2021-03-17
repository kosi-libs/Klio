plugins {
    id("org.kodein.root")
}

allprojects {
    group = "org.kodein.memory"
    version = "0.8.1"

    repositories {
        mavenCentral()
    }
}

val kotlinxSerializationVer by extra { "1.1.0" }