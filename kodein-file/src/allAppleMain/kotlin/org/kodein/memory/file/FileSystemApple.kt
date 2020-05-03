package org.kodein.memory.file

import platform.Foundation.NSFileManager
import platform.Foundation.temporaryDirectory

actual object FileSystem {

    internal actual val pathSeparator: String = "/"

    actual val tempDirectory: Path get() = Path(NSFileManager.defaultManager.temporaryDirectory.path!!)

    actual var currentDirectory: Path
        get() = Path(NSFileManager.defaultManager.currentDirectoryPath)
        set(value) { NSFileManager.defaultManager.changeCurrentDirectoryPath(value.path) }

    actual val roots: List<Path> = listOf(Path("/"))

}
