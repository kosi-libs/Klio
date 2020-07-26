plugins {
    id("org.kodein.library.mpp-with-android")
}

kodein {
    kotlin {

        val otherPosix = kodeinSourceSets.new("otherPosix")

        val os = org.gradle.internal.os.OperatingSystem.current()
        if (os.isLinux || os.isMacOsX || os.isUnix) {
            cpFixes.update("nativeHost") {
                it.copy(intermediateSourceSets = it.intermediateSourceSets + otherPosix)
            }
        }

        common.main.dependencies {
            api(project(":kodein-memory"))
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

        add(kodeinTargets.native.allEmbeddedLinux + kodeinTargets.native.linuxX64 + kodeinTargets.native.macosX64) {
            dependsOn(otherPosix)
        }

        add(kodeinTargets.native.allDarwin)

        // Remove mingwX64 from allPosix
        add(kodeinTargets.native.mingwX64.copy(dependencies = listOf(kodeinSourceSets.allNative)))

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}

afterEvaluate {
    tasks.withType<com.android.build.gradle.tasks.factory.AndroidUnitTest>().all {
        enabled = false
    }
}

kodeinUpload {
    name = "Kodein-File"
    description = "Kodein File Library"
}