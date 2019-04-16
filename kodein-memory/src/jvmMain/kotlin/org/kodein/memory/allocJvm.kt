package org.kodein.memory

import java.nio.ByteBuffer

actual fun allocNativeKBuffer(capacity: Int): Allocation = ManagedAllocation(JvmNioKBuffer(ByteBuffer.allocateDirect(capacity)))
