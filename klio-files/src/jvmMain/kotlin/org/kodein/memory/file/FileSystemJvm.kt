package org.kodein.memory.file

import java.io.File
import java.lang.IllegalStateException

public actual object FileSystem {
    internal actual val pathSeparator: String = File.separator

    public actual val tempDirectory: Path get() = Path(System.getProperty("java.io.tmpdir"))

    public actual fun workingDir(): Path = Path(System.getProperty("user.dir") ?: throw IllegalStateException("No user.dir system property"))
    public actual fun changeWorkingDir(path: Path) { System.setProperty("user.dir", path.path) }

    public actual val roots: List<Path> = File.listRoots().map { Path(it.absolutePath) }
}
