package org.kodein.memory.file

import platform.Foundation.NSFileManager
import platform.Foundation.temporaryDirectory

public actual object FileSystem {

    internal actual val pathSeparator: String = "/"

    public actual val tempDirectory: Path get() = Path(NSFileManager.defaultManager.temporaryDirectory.path!!)

    public actual var currentDirectory: Path
        get() = Path(NSFileManager.defaultManager.currentDirectoryPath)
        set(value) { NSFileManager.defaultManager.changeCurrentDirectoryPath(value.path) }

    public actual val roots: List<Path> = listOf(Path("/"))

}
