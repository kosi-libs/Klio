package org.kodein.memory.file

import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import org.kodein.memory.io.IOException
import platform.posix.PATH_MAX
import platform.windows.*

public actual fun Path.isAbsolute(): Boolean = PathIsRelativeW(path) == 0
