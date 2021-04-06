plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        val appleNative = kodeinSourceSets.new("appleNative", listOf(kodeinSourceSets.allNative))
        val appleNativeTargets = kodeinTargets.native.allDarwin + kodeinTargets.native.macosX64
        appleNativeTargets.forEach {
            it.dependencies.add(appleNative)
        }

        val otherNative = kodeinSourceSets.new("appleNative", listOf(kodeinSourceSets.allNative))
        val otherNativeTargets = kodeinTargets.native.all - appleNativeTargets
        otherNativeTargets.forEach {
            it.dependencies.add(otherNative)
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

        add(otherNativeTargets) {
//            val targetName = target.name
//            mainCompilation.cinterops.create("libcrypto") {
//                tasks[interopProcessingTaskName].dependsOn(":kodein-memory-crypto:nativeLib:buildOpenSSL")
//                includeDirs {
//                    headerFilterOnly("$projectDir/nativeLib/build/$targetName/include")
//                }
//                extraOpts("-libraryPath", "$projectDir/nativeLib/build/$targetName/lib")
//            }
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