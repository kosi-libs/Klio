package org.kodein.memory.file


public actual fun Path.isAbsolute(): Boolean = asNio().isAbsolute

public fun Path.asNio(): java.nio.file.Path = java.nio.file.Paths.get(path)
