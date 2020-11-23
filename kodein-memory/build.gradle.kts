plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

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

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.enableLanguageFeature("InlineClasses")
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
    name = "Kodein-Memory"
    description = "Kodein Memory Library"
}
