package org.kodein.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap

actual fun allocNative(capacity: Int): Allocation {
    val pointer = nativeHeap.allocArray<ByteVar>(capacity)
    return NativeAllocation(CPointerKBuffer(pointer, capacity)) { nativeHeap.free(pointer) }
}
