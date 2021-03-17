package org.kodein.memory.io

import java.nio.ByteBuffer


@Suppress("FunctionName")
public fun ByteBufferMemory(byteBuffer: ByteBuffer): Memory =
    when {
        byteBuffer.isDirect -> DirectByteBufferMemory(byteBuffer)
        byteBuffer.hasArray() -> ByteArrayMemory(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.limit())
        else -> error("ByteBuffer is neither direct, nor backed by array")
    }
