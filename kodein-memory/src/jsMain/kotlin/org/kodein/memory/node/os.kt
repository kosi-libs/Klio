package org.kodein.memory.node

@JsModule("fs")
@JsNonModule
public external object NodeOs {
    public fun tmpdir(): String
}
