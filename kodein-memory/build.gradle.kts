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

            dependsOn(allNonJvm)
        }

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}
