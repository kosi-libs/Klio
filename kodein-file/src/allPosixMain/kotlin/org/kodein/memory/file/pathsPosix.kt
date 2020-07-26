package org.kodein.memory.file

public actual fun Path.isAbsolute(): Boolean = path.startsWith("/")
