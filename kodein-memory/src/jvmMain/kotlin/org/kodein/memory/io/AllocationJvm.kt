package org.kodein.memory.io

import java.nio.ByteBuffer

actual fun Allocation.Allocations.native(capacity: Int): Allocation = JvmNioKBuffer(ByteBuffer.allocateDirect(capacity)).asManagedAllocation()
