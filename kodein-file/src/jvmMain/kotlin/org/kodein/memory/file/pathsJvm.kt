package org.kodein.memory.file

import java.io.File

actual fun Path.isAbsolute(): Boolean = File(path).isAbsolute

actual fun Path.resolve(path: Path): Path = Path(File(this.path).resolve(path.path).path)

actual fun Path.normalize(): Path = Path(File(path).normalize().path)

actual fun Path.parent(): Path = Path(File(path).parent)
