package org.kodein.memory.system


public actual object Environment {

    public actual fun getVariable(name: String): String = System.getenv(name) ?: throw NoSuchElementException("No environment variable $name")

    public actual fun findVariable(name: String): String? = System.getenv(name)

}
