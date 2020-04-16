package org.kodein.memory.file


inline class Path(val path: String) {

    constructor(vararg values: String) : this(values.joinToString(separator))

    companion object {
        val separator: String get() = FileSystem.pathSeparator
    }

    override fun toString() = path
}

expect fun Path.isAbsolute(): Boolean

fun Path.resolve(path: Path): Path {
    require(!path.isAbsolute()) { "Cannot resolve absolute path $path on top of $this" }
    return Path(buildString {
        append(this@resolve.path)
        if (!endsWith(Path.separator)) append(Path.separator)
        append(path.path)
    })
}

fun Path.resolve(vararg values: String) = resolve(Path(*values))


fun Path.toAbsolute(): Path {
    if (isAbsolute()) return this
    return FileSystem.currentDirectory.resolve(this)
}

fun Path.normalize(): Path {
    val isAbsolute = isAbsolute()

    val segments = path.split(Path.separator).toMutableList()
    if (segments.size == 1)
        return this

    var removed: Boolean
    do {
        removed = false
        for (i in 0..segments.lastIndex) {
            if (segments[i] == ".") {
                segments.removeAt(i)
                removed = true
                break
            }
            if (i >= 1 && segments[i] == ".." && segments[i - 1] != "..") {
                segments.removeAt(i)
                if (i != 1 || !isAbsolute) segments.removeAt(i - 1)
                removed = true
                break
            }
        }
    } while (removed)

    if (segments.isEmpty()) return Path(".")

    if (isAbsolute && segments.size == 1) {
        return Path((segments + "").joinToString(Path.separator))
    }
    return Path(segments.joinToString(Path.separator))
}

fun Path.parent() = resolve("..").normalize()

val Path.name get() = path.split(Path.separator).last()