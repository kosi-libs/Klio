package org.kodein.memory.file

import android.content.Context
import java.io.File
import java.lang.IllegalStateException

actual object FileSystem {
    private var registeredContext: Context? = null
    private val context: Context get() = registeredContext ?: throw IllegalStateException("Please use FileSystem.registerContext first")

    fun registerContext(ctx: Context) {
        registeredContext = ctx.applicationContext
    }

    internal actual val pathSeparator: String = File.separator

    actual val tempDirectory: Path get() = Path(context.cacheDir.absolutePath)

    actual var currentDirectory: Path
        get() = Path(System.getProperty("user.dir") ?: throw IllegalStateException("No user.dir system property"))
        set(value) { System.setProperty("user.dir", value.path) }

    actual val roots: List<Path> = File.listRoots().map { Path(it.absolutePath) }
}
