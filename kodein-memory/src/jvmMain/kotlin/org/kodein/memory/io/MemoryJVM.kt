package org.kodein.memory.io

import java.nio.ByteBuffer

public fun Memory.Companion.wrap(byteBuffer: ByteBuffer): Memory = ByteBufferMemory(byteBuffer)
