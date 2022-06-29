package org.kodein.memory.crypto.lib

import org.khronos.webgl.Uint8Array


internal external interface JsDigest {
    fun update(value: Uint8Array)
    fun digest(): Uint8Array
}
