package org.kodein.memory.io

import java.nio.ByteBuffer

public actual fun Allocation.Companion.native(size: Int): Allocation =
    DirectByteBufferMemory(ByteBuffer.allocateDirect(size)).asManagedAllocation()
