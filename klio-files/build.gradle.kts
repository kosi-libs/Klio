plugins {
    id("org.kodein.library.mpp-with-android")
}

kodein {
    kotlin {

        val otherPosix = kodeinSourceSets.new("otherPosix", listOf(kodeinSourceSets.allPosix))
        val otherPosixTargets = kodeinTargets.native.allPosix - kodeinTargets.native.allDarwin
        otherPosixTargets.forEach {
            it.dependencies.add(otherPosix)
        }

        common.main.dependencies {
            api(project(":klio"))
        }

        add(kodeinTargets.jvm.jvm) {
            target.setCompileClasspath()

            test.dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect")
            }
        }

        add(kodeinTargets.jvm.android) {
            test.dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
            }
        }

        add(otherPosixTargets)
        add(kodeinTargets.native.allDarwin)
        add(kodeinTargets.native.mingwX64)
    }
}

afterEvaluate {
    tasks.withType<com.android.build.gradle.tasks.factory.AndroidUnitTest>().all {
        enabled = false
    }
}

kodeinUpload {
    name = "Klio-Files"
    description = "Kotlin Multiplatform File Library"
}