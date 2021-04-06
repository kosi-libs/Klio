package org.kodein.memory.io

import java.nio.ByteBuffer


public actual typealias PlatformNativeMemory = DirectByteBufferMemory

public actual fun Allocation.Companion.native(size: Int): PlatformNativeAllocation =
    MemoryAllocation(DirectByteBufferMemory(ByteBuffer.allocateDirect(size))) {}
