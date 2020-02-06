package org.kodein.memory.io

import java.nio.ByteBuffer

fun KBuffer.Companion.wrap(byteBuffer: ByteBuffer) = JvmNioKBuffer(byteBuffer).also {
    it.position = byteBuffer.position()
    it.limit = byteBuffer.limit()
}
