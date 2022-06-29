package org.kodein.memory.io

import java.nio.Buffer
import java.nio.ByteBuffer


@Suppress("FunctionName")
public fun ByteBufferMemory(byteBuffer: ByteBuffer): Memory =
    when {
        byteBuffer.isDirect -> DirectByteBufferMemory(byteBuffer)
        byteBuffer.hasArray() -> ByteArrayMemory(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.jLimit)
        else -> error("ByteBuffer is neither direct, nor backed by array")
    }

internal inline fun <R> ByteBuffer.tmp(g: ByteBuffer.() -> Int, s: ByteBuffer.(Int) -> Unit, new: Int, block: () -> R): R {
    val old = g()
    if (old == new) return block()

    s(new)
    try {
        return block()
    } finally {
        s(old)
    }
}

// Fixing https://github.com/Kodein-Framework/Kodein-DB/issues/33
internal var Buffer.jPosition: Int ; get() = position() ; set(value) { position(value) }
internal var Buffer.jLimit: Int ; get() = limit() ; set(value) { limit(value) }

internal inline fun <R> ByteBuffer.withLimit(newLimit: Int, block: () -> R): R = tmp<R>(Buffer::jLimit::get, Buffer::jLimit::set, newLimit, block)
internal inline fun <R> ByteBuffer.withPosition(newPosition: Int, block: () -> R): R = tmp<R>(Buffer::jPosition::get, Buffer::jPosition::set, newPosition, block)

public fun Memory.Companion.wrap(byteBuffer: ByteBuffer): Memory = ByteBufferMemory(byteBuffer)

public fun ByteBuffer.asMemory(): Memory = Memory.wrap(this)
