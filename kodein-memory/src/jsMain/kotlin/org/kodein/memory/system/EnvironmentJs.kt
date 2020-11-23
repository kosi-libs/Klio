package org.kodein.memory.system


public actual object Environment {

    public actual fun getVariable(name: String): String = error("Environment variables are not supported in JS")

    public actual fun findVariable(name: String): String? = null

    public actual fun allVariables(): Map<String, String> = emptyMap()

}
