plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        common {
        }

        add(kodeinTargets.jvm) {
            target.setCompileClasspath()

            test.dependencies {
                implementation("org.jetbrains.kotlin:kotlin-reflect")
            }
        }

//        add(kodeinTargets.js)

        add(kodeinTargets.native.allNonWeb) {

            mainCompilation.cinterops.apply {
                create("bits")
            }

        }
    }
}
