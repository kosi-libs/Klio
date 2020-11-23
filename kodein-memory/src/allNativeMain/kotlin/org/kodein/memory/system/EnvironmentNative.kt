package org.kodein.memory.system

import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import org.kodein.memory.internal.environ
import platform.posix.getenv


public actual object Environment {

    public actual fun getVariable(name: String): String = getenv(name)?.toKString() ?: throw NoSuchElementException("No environment variable $name")

    public actual fun findVariable(name: String): String? = getenv(name)?.toKString()

    public actual fun allVariables(): Map<String, String> {
        val env = environ ?: return emptyMap()
        val list = HashMap<String, String>()
        var i = 0
        while (true) {
            val (name, value) = env[i]?.toKString()?.split('=') ?: break
            list[name] = value
            ++i
        }
        return list
    }

}
