package org.kodein.memory.system


public expect object Environment {

    public fun getVariable(name: String): String

    public fun findVariable(name: String): String?

}
