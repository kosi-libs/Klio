package org.kodein.memory.file

import java.io.File

actual fun Path.isAbsolute(): Boolean = File(path).isAbsolute
