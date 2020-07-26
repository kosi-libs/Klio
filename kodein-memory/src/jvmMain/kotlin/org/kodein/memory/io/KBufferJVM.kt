package org.kodein.memory.io

import java.nio.ByteBuffer

public fun KBuffer.Companion.wrap(byteBuffer: ByteBuffer): JvmNioKBuffer = JvmNioKBuffer(byteBuffer).also {
    it.position = byteBuffer.position()
    it.limit = byteBuffer.limit()
}
