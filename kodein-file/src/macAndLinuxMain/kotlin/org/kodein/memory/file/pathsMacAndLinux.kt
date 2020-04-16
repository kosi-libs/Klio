package org.kodein.memory.file

actual fun Path.isAbsolute(): Boolean = path.startsWith("/")
