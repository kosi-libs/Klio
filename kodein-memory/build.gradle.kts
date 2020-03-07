plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        val allNonJvmMain = sourceSets.create("allNonJvmMain") {
            dependsOn(common.main)
        }

        add(kodeinTargets.jvm.jvm) {
            target.setCompileClasspath()

            test.dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect")
            }
        }

        add(kodeinTargets.js.js) {
            main.dependsOn(allNonJvmMain)
        }

        add(kodeinTargets.native.allPosix) {
            mainCompilation.cinterops.create("bits")

            main.dependsOn(allNonJvmMain)
        }

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }
    }
}
