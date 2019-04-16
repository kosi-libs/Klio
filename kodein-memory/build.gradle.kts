plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        common {
        }

        add(kodeinTargets.jvm) {
            target.setCompileClasspath()
        }

//        add(kodeinTargets.js)

        add(kodeinTargets.native.allNonWeb) {

            mainCompilation.cinterops.apply {
                create("bits")
            }

        }
    }
}
