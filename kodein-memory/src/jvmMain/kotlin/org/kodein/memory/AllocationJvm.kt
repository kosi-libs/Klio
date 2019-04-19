package org.kodein.memory

import java.nio.ByteBuffer

actual fun Allocation.Allocations.native(capacity: Int): Allocation = ManagedAllocation(JvmNioKBuffer(ByteBuffer.allocateDirect(capacity)))
