package org.kodein.memory.node

import org.khronos.webgl.Uint8Array

@JsModule("fs")
@JsNonModule
public external object NodeFs {
    public interface Stats {
        public val size: Int
    }
    public fun openSync(path: String, flags: String = definedExternally): Int
    public fun fstatSync(fd: Int): Stats
    public fun readSync(fd: Int, buffer: Uint8Array, offset: Int, length: Int): Int
    public fun writeSync(fd: Int, buffer: Uint8Array, offset: Int, length: Int): Int
    public fun fsyncSync(fd: Int)
    public fun closeSync(fd: Int)
}
