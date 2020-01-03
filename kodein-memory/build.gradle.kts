plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-atomicfu")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val serializationVer = "0.14.0"

        common.main.dependencies {
            compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationVer")
        }

        val allNonJvmMain = sourceSets.create("allNonJvmMain") {
            dependsOn(common.main)
        }

        add(kodeinTargets.jvm.jvm) {
            target.setCompileClasspath()

            main.dependencies {
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVer")
            }

            test.dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect")
            }
        }

        add(kodeinTargets.js.js) {
            main.dependencies {
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:$serializationVer")
            }

            main.dependsOn(allNonJvmMain)
        }

        add(kodeinTargets.native.allDesktop + kodeinTargets.native.allIos) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializationVer")
            }

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
