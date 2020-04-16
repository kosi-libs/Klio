package org.kodein.memory.node

@JsModule("fs")
@JsNonModule
external object NodeOs {
    fun tmpdir(): String
}
