package org.kodein.memory.file

import java.io.File

public actual fun Path.isAbsolute(): Boolean = File(path).isAbsolute
