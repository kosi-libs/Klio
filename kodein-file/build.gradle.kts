plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        val macAndLinux = kodeinSourceSets.new("macAndLinux")

        val os = org.gradle.internal.os.OperatingSystem.current()
        if (os.isLinux || os.isMacOsX || os.isUnix) {
            cpFixes.update("nativeHost") {
                it.copy(intermediateSourceSets = it.intermediateSourceSets + macAndLinux)
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

        add(kodeinTargets.native.allPosix)

        add(kodeinTargets.native.allLinux + kodeinTargets.native.macosX64) {
            dependsOn(macAndLinux)
        }

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}
