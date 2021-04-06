package org.kodein.memory.io

import java.nio.ByteBuffer


@Suppress("FunctionName")
public fun ByteBufferMemory(byteBuffer: ByteBuffer): Memory =
    when {
        byteBuffer.isDirect -> DirectByteBufferMemory(byteBuffer)
        byteBuffer.hasArray() -> ByteArrayMemory(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.limit())
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

internal inline fun <R> ByteBuffer.limit(newLimit: Int, block: () -> R): R = tmp<R>(ByteBuffer::limit, { limit(it) }, newLimit, block)
internal inline fun <R> ByteBuffer.position(newPosition: Int, block: () -> R): R = tmp<R>(ByteBuffer::position, { position(it) }, newPosition, block)

public fun Memory.Companion.wrap(byteBuffer: ByteBuffer): Memory = ByteBufferMemory(byteBuffer)

public fun ByteBuffer.asMemory(): Memory = Memory.wrap(this)
