package org.kodein.memory.file

public expect object FileSystem {
    internal val pathSeparator: String

    public val tempDirectory: Path

    public fun workingDir(): Path
    public fun changeWorkingDir(path: Path)

    public val roots: List<Path>
}