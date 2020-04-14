package org.kodein.memory.file

expect object FileSystem {
    internal val pathSeparator: String

    val tempDirectory: Path

    var currentDirectory: Path

    val roots: List<Path>
}