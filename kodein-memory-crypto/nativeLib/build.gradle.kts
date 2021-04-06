

fun PatternFilterable.includeCFiles() = include { it.file.extension == "c" || it.file.extension == "h" }

tasks.create<Exec>("buildOpenSSL") {
    val os = org.gradle.internal.os.OperatingSystem.current()
    val (configuration, target) = when {
        os.isMacOsX -> "darwin64-x86_64-cc" to "macosX64"
        os.isLinux -> "linux-x86_64" to "linuxX64"
        os.isWindows -> "mingw64" to "mingwX64"
        else -> error("Unsupported OS")
    }

    group = "build"
    workingDir = projectDir
    executable = projectDir.resolve("buildOpenSSL.sh").absolutePath
    args(configuration, target)

    inputs.files(
        fileTree("$projectDir/openssl/ssl").includeCFiles(),
        fileTree("$projectDir/openssl/crypto").includeCFiles(),
        fileTree("$projectDir/openssl/include").includeCFiles().exclude { it.file.name.endsWith("conf.h") }
    )

    outputs.files(
        file("$buildDir/$target/lib/libssl.a"),
        file("$buildDir/$target/lib/libcrypto.a"),
        fileTree("$buildDir/$target/include/openssl")
    )
}
