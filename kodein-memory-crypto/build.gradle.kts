plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        val appleNative = kodeinSourceSets.new("appleNative", listOf(kodeinSourceSets.allPosix))
        val appleNativeTargets = kodeinTargets.native.allDarwin + kodeinTargets.native.macosX64
        appleNativeTargets.forEach {
            it.dependencies.add(appleNative)
        }

        val linuxNative = kodeinSourceSets.new("linuxNative", listOf(kodeinSourceSets.allPosix))
        val linuxNativeTargets = kodeinTargets.native.allEmbeddedLinux + kodeinTargets.native.linuxX64
        linuxNativeTargets.forEach {
            it.dependencies.add(linuxNative)
        }

        common.main.dependencies {
            api(project(":kodein-memory"))
        }

        add(kodeinTargets.jvm.jvm) {
            target.setCompileClasspath()
        }

        add(appleNativeTargets) {
            mainCompilation.cinterops.create("appleCoreCrypto")
        }

        add(linuxNativeTargets) {
            mainCompilation.cinterops.create("linuxCrypto")
        }

        add(kodeinTargets.js.js) {
            main.dependencies {
                implementation(npm("sha.js", "2.4.11"))
                implementation(npm("create-hmac", "1.1.7"))
                implementation(npm("safe-buffer", "5.2.1"))
                implementation(npm("browserify-aes", "1.2.0"))
                implementation(npm("pbkdf2", "3.1.1"))
//                implementation(npm("jssha", "3.2.0"))
//                implementation(npm("aes-js", "3.1.2"))
            }
        }

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }
    }
}

kodeinUpload {
    name = "Kodein-Memory-Crypto"
    description = "Kodein Crypto Library"
}