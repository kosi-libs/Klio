plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        val allNonJvmMain = sourceSets.create("allNonJvmMain") {
            dependsOn(common.main)
        }

        add(kodeinTargets.jvm) {
            target.setCompileClasspath()

            test.dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect")
            }
        }

        add(kodeinTargets.js) {
            main.dependsOn(allNonJvmMain)
        }

        add(kodeinTargets.native.allNonWeb) {
            mainCompilation.cinterops.apply {
                create("bits")
            }

            main.dependsOn(allNonJvmMain)
        }

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }
    }
}

afterEvaluate {
    tasks.withType<AbstractTestTask>().forEach {
        it.outputs.upToDateWhen { false }
    }
}
