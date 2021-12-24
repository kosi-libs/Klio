plugins {
    id("org.kodein.root")
}

allprojects {
    group = "org.kodein.memory"
    version = "0.11.0"

    repositories {
        mavenCentral()
    }
}

val kotlinxSerializationVer by extra { "1.3.2" }
