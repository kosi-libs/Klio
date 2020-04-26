plugins {
    id("org.kodein.library.mpp-with-android")
}

kodein {
    kotlin {

        val nativePosix = kodeinSourceSets.new("nativePosix")

        val os = org.gradle.internal.os.OperatingSystem.current()
        if (os.isLinux || os.isMacOsX || os.isUnix) {
            cpFixes.update("nativeHost") {
                it.copy(intermediateSourceSets = it.intermediateSourceSets + nativePosix)
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
            }
        }

        add(kodeinTargets.native.allPosix - kodeinTargets.native.mingwX64) {
            dependsOn(nativePosix)
        }

        add(kodeinTargets.native.mingwX64)

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}
