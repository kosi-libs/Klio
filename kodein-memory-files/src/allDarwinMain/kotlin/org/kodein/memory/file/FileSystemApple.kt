package org.kodein.memory.file

import platform.Foundation.NSFileManager
import platform.Foundation.temporaryDirectory

public actual object FileSystem {

    internal actual val pathSeparator: String = "/"

    public actual val tempDirectory: Path get() = Path(NSFileManager.defaultManager.temporaryDirectory.path!!)

    public actual fun workingDir(): Path = Path(NSFileManager.defaultManager.currentDirectoryPath)
    public actual fun changeWorkingDir(path: Path) { NSFileManager.defaultManager.changeCurrentDirectoryPath(path.path) }

    public actual val roots: List<Path> = listOf(Path("/"))

}
