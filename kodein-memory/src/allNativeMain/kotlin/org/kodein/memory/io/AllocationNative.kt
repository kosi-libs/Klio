package org.kodein.memory.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap

public actual fun Allocation.Allocations.native(capacity: Int): Allocation {
    val pointer = nativeHeap.allocArray<ByteVar>(capacity)
    return NativeAllocation(CPointerKBuffer(pointer, capacity)) { nativeHeap.free(pointer) }
}
