package org.kodein.memory.node

import org.khronos.webgl.Uint8Array

@JsModule("fs")
@JsNonModule
external object NodeFs {
    interface Stats {
        val size: Int
    }
    fun openSync(path: String, flags: String = definedExternally): Int
    fun fstatSync(fd: Int): Stats
    fun readSync(fd: Int, buffer: Uint8Array, offset: Int, length: Int): Int
    fun writeSync(fd: Int, buffer: Uint8Array, offset: Int, length: Int): Int
    fun fsyncSync(fd: Int)
    fun closeSync(fd: Int)
}
