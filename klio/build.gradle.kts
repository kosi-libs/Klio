plugins {
    id("org.kodein.library.mpp")
    kotlin("plugin.serialization")
}

kodein {
    kotlin {

        val kotlinxSerializationVer: String by rootProject.extra

        common.main.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVer")
        }

        common.test.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVer")
        }

        val allNonJvm = kodeinSourceSets.new("allNonJvm")

        add(kodeinTargets.jvm.jvm) {
            target.setCompileClasspath()

            test.dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect")
            }
        }

        add(kodeinTargets.js.js) {
            dependsOn(allNonJvm)
        }

        add(kodeinTargets.native.allPosix) {
            mainCompilation.cinterops.create("bits")
            mainCompilation.cinterops.create("environ")

            dependsOn(allNonJvm)
        }

        add(kodeinTargets.native.mingwX64) {
            mainCompilation.cinterops.create("bits")
            mainCompilation.cinterops.create("environ_windows")

            dependsOn(allNonJvm)
        }

        sourceSets.all {
            languageSettings.optIn("kotlin.Experimental")
        }
    }
}

afterEvaluate {
    tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeHostTest> {
        environment("TEST_VARIABLE", "Working!")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest> {
        environment("TEST_VARIABLE", "Working!")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest> {
        environment("SIMCTL_CHILD_TEST_VARIABLE", "Working!")
    }
}

kodeinUpload {
    name = "Klio"
    description = "Kotlin Multiplatform Low-level I/O Library"
}
