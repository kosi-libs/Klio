package org.kodein.memory.system

import kotlinx.cinterop.toKString
import platform.posix.getenv


public actual object Environment {

    public actual fun getVariable(name: String): String = getenv(name)?.toKString() ?: throw NoSuchElementException("No environment variable $name")

    public actual fun findVariable(name: String): String? = getenv(name)?.toKString()

}
