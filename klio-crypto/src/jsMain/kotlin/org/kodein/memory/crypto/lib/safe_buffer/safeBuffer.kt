@file:JsModule("safe-buffer")
@file:JsNonModule
package org.kodein.memory.crypto.lib.safe_buffer

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

internal external class Buffer : Uint8Array {
    fun write(string: String, offset: Number = definedExternally, length: Number = definedExternally, encoding: String = definedExternally): Number
    fun toString(encoding: String = definedExternally, start: Number = definedExternally, end: Number = definedExternally): String
    fun compare(otherBuffer: Buffer, targetStart: Number = definedExternally, targetEnd: Number = definedExternally, sourceStart: Number = definedExternally, sourceEnd: Number = definedExternally): Number
    fun copy(targetBuffer: Buffer, targetStart: Number = definedExternally, sourceStart: Number = definedExternally, sourceEnd: Number = definedExternally): Number
    fun slice(start: Number = definedExternally, end: Number = definedExternally): Buffer
    fun writeUIntLE(value: Number, offset: Number, byteLength: Number, noAssert: Boolean = definedExternally): Number
    fun writeUIntBE(value: Number, offset: Number, byteLength: Number, noAssert: Boolean = definedExternally): Number
    fun writeIntLE(value: Number, offset: Number, byteLength: Number, noAssert: Boolean = definedExternally): Number
    fun writeIntBE(value: Number, offset: Number, byteLength: Number, noAssert: Boolean = definedExternally): Number
    fun readUIntLE(offset: Number, byteLength: Number, noAssert: Boolean = definedExternally): Number
    fun readUIntBE(offset: Number, byteLength: Number, noAssert: Boolean = definedExternally): Number
    fun readIntLE(offset: Number, byteLength: Number, noAssert: Boolean = definedExternally): Number
    fun readIntBE(offset: Number, byteLength: Number, noAssert: Boolean = definedExternally): Number
    fun readUInt8(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readUInt16LE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readUInt16BE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readUInt32LE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readUInt32BE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readInt8(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readInt16LE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readInt16BE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readInt32LE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readInt32BE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readFloatLE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readFloatBE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readDoubleLE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun readDoubleBE(offset: Number, noAssert: Boolean = definedExternally): Number
    fun swap16(): Buffer
    fun swap32(): Buffer
    fun swap64(): Buffer
    fun writeUInt8(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeUInt16LE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeUInt16BE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeUInt32LE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeUInt32BE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeInt8(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeInt16LE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeInt16BE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeInt32LE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeInt32BE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeFloatLE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeFloatBE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeDoubleLE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun writeDoubleBE(value: Number, offset: Number, noAssert: Boolean = definedExternally): Number
    fun fill(value: Any, offset: Number = definedExternally, end: Number = definedExternally): Buffer /* this */
    fun indexOf(value: String, byteOffset: Number = definedExternally, encoding: String = definedExternally): Number
    fun indexOf(value: Number, byteOffset: Number = definedExternally, encoding: String = definedExternally): Number
    fun indexOf(value: Buffer, byteOffset: Number = definedExternally, encoding: String = definedExternally): Number
    fun lastIndexOf(value: String, byteOffset: Number = definedExternally, encoding: String = definedExternally): Number
    fun lastIndexOf(value: Number, byteOffset: Number = definedExternally, encoding: String = definedExternally): Number
    fun lastIndexOf(value: Buffer, byteOffset: Number = definedExternally, encoding: String = definedExternally): Number
    fun includes(value: String, byteOffset: Number = definedExternally, encoding: String = definedExternally): Boolean
    fun includes(value: Number, byteOffset: Number = definedExternally, encoding: String = definedExternally): Boolean
    fun includes(value: Buffer, byteOffset: Number = definedExternally, encoding: String = definedExternally): Boolean
    constructor(str: String, encoding: String = definedExternally)
    constructor(size: Number)
    constructor(array: Uint8Array)
    constructor(arrayBuffer: ArrayBuffer)
    constructor(array: Array<Any>)
    constructor(buffer: Buffer)

    companion object {
        fun from(array: Array<Any>): Buffer
        fun from(arrayBuffer: ArrayBuffer, byteOffset: Number = definedExternally, length: Number = definedExternally): Buffer
        fun from(buffer: Buffer): Buffer
        fun from(str: String, encoding: String = definedExternally): Buffer
        fun isBuffer(obj: Any): Boolean
        fun isEncoding(encoding: String): Boolean
        fun byteLength(string: String, encoding: String = definedExternally): Number
        fun concat(list: Array<Buffer>, totalLength: Number = definedExternally): Buffer
        fun compare(buf1: Buffer, buf2: Buffer): Number
        fun alloc(size: Number): Buffer
        fun alloc(size: Number, fill: String, encoding: String = definedExternally): Buffer
        fun alloc(size: Number, fill: Buffer, encoding: String = definedExternally): Buffer
        fun alloc(size: Number, fill: Number, encoding: String = definedExternally): Buffer
        fun allocUnsafe(size: Number): Buffer
        fun allocUnsafeSlow(size: Number): Buffer
    }
}