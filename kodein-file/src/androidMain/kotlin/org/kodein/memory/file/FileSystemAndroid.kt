package org.kodein.memory.file

import android.content.Context
import java.io.File
import java.lang.IllegalStateException

public actual object FileSystem {
    private var registeredContext: Context? = null
    private val context: Context get() = registeredContext ?: throw IllegalStateException("Please use FileSystem.registerContext first")

    public fun registerContext(ctx: Context) {
        registeredContext = ctx.applicationContext
    }

    internal actual val pathSeparator: String = File.separator

    public actual val tempDirectory: Path get() = Path(context.cacheDir.absolutePath)

    public actual var currentDirectory: Path
        get() = Path(System.getProperty("user.dir") ?: throw IllegalStateException("No user.dir system property"))
        set(value) { System.setProperty("user.dir", value.path) }

    public actual val roots: List<Path> = File.listRoots().map { Path(it.absolutePath) }
}
