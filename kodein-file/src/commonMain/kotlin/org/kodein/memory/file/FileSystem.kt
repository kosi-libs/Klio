package org.kodein.memory.file

public expect object FileSystem {
    internal val pathSeparator: String

    public val tempDirectory: Path

    public var currentDirectory: Path

    public val roots: List<Path>
}