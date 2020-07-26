package org.kodein.memory.io

import java.nio.ByteBuffer

public actual fun Allocation.Allocations.native(capacity: Int): Allocation = JvmNioKBuffer(ByteBuffer.allocateDirect(capacity)).asManagedAllocation()
