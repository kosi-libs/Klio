package org.kodein.memory

import java.nio.ByteBuffer

actual fun allocNative(capacity: Int): Allocation = HeapAllocation(JvmNioKBuffer(ByteBuffer.allocateDirect(capacity)))
