package org.kodein.memory.crypto

import org.kodein.memory.Closeable
import org.kodein.memory.io.*
import org.kodein.memory.use


public interface DigestWriteable : Writeable, Closeable {
    public val digestSize: Int
    public fun digestInto(dst: ByteArray, dstOffset: Int = 0)
    public fun digestInto(dst: Memory)
    public fun digestInto(dst: Writeable)
    public fun reset()

    public companion object
}

public fun DigestWriteable.digestBytes(): ByteArray = ByteArray(digestSize).also { digestInto(Memory.wrap(it)) }

public enum class DigestAlgorithm { SHA1, SHA224, SHA256, SHA384, SHA512 }

public expect fun DigestWriteable.Companion.newInstance(algorithm: DigestAlgorithm): DigestWriteable

public inline fun ByteArray.putHashOf(algorithm: DigestAlgorithm, offset: Int, block: Writeable.() -> Unit) {
    DigestWriteable.newInstance(algorithm).use {
        it.block()
        it.digestInto(this, offset)
    }
}

public inline fun Memory.putHashOf(index: Int, algorithm: DigestAlgorithm, block: Writeable.() -> Unit) {
    DigestWriteable.newInstance(algorithm).use {
        it.block()
        it.digestInto(this.sliceAt(index))
    }
}

public inline fun Writeable.writeHashOf(algorithm: DigestAlgorithm, block: Writeable.() -> Unit) {
    DigestWriteable.newInstance(algorithm).use {
        it.block()
        it.digestInto(this)
    }
}

public inline fun DigestAlgorithm.hashOf(block: Writeable.() -> Unit): ByteArray {
    DigestWriteable.newInstance(this).use {
        it.block()
        return it.digestBytes()
    }
}

public fun DigestAlgorithm.hashOf(src: ByteArray, offset: Int, length: Int): ByteArray = hashOf { writeBytes(src, offset, length) }
public fun DigestAlgorithm.hashOf(src: ByteArray): ByteArray = hashOf { writeBytes(src) }
public fun DigestAlgorithm.hashOf(src: ReadMemory): ByteArray = hashOf { writeBytes(src) }
public fun DigestAlgorithm.hashOf(src: Readable, length: Int): ByteArray = hashOf { writeBytes(src, length) }
public fun DigestAlgorithm.hashOf(src: CursorReadable): ByteArray = hashOf { writeBytes(src) }

public expect fun DigestWriteable.Companion.newHmacInstance(algorithm: DigestAlgorithm, key: ReadMemory): DigestWriteable

public inline fun ByteArray.putHmacOf(offset: Int, algorithm: DigestAlgorithm, key: ReadMemory, block: Writeable.() -> Unit) {
    DigestWriteable.newHmacInstance(algorithm, key).use {
        it.block()
        it.digestInto(this, offset)
    }
}

public inline fun Memory.putHmacOf(index: Int, algorithm: DigestAlgorithm, key: ReadMemory, block: Writeable.() -> Unit) {
    DigestWriteable.newHmacInstance(algorithm, key).use {
        it.block()
        it.digestInto(this.sliceAt(index))
    }
}

public inline fun Writeable.writeHmacOf(algorithm: DigestAlgorithm, key: ReadMemory, block: Writeable.() -> Unit) {
    DigestWriteable.newHmacInstance(algorithm, key).use {
        it.block()
        it.digestInto(this)
    }
}

public inline fun DigestAlgorithm.hmacOf(key: ReadMemory, block: Writeable.() -> Unit): ByteArray {
    DigestWriteable.newHmacInstance(this, key).use {
        it.block()
        return it.digestBytes()
    }
}

public fun DigestAlgorithm.hmacOf(key: ReadMemory, src: ByteArray, offset: Int, length: Int): ByteArray = hmacOf(key) { writeBytes(src, offset, length) }
public fun DigestAlgorithm.hmacOf(key: ReadMemory, src: ByteArray): ByteArray = hmacOf(key) { writeBytes(src) }
public fun DigestAlgorithm.hmacOf(key: ReadMemory, src: ReadMemory): ByteArray = hmacOf(key) { writeBytes(src) }
public fun DigestAlgorithm.hmacOf(key: ReadMemory, src: Readable, length: Int): ByteArray = hmacOf(key) { writeBytes(src, length) }
public fun DigestAlgorithm.hmacOf(key: ReadMemory, src: CursorReadable): ByteArray = hmacOf(key) { writeBytes(src) }
