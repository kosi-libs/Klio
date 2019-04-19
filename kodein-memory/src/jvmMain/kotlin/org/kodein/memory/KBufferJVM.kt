package org.kodein.memory

import java.nio.ByteBuffer

fun KBuffer.Companion.wrap(byteBuffer: ByteBuffer) = JvmNioKBuffer(byteBuffer)
