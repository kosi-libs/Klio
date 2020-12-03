plugins {
    id("org.kodein.root")
}

allprojects {
    group = "org.kodein.memory"
    version = "0.5.0"

    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }
}

val kotlinxSerializationVer by extra { "1.0.1" }

kodeinPublications {
    repo = "Kodein-Memory"
}
