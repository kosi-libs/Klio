package org.kodein.memory.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap

public actual fun Allocation.Companion.native(size: Int): Allocation {
    val pointer = nativeHeap.allocArray<ByteVar>(size)
    return NativeAllocation(CPointerMemory(pointer, size)) { nativeHeap.free(pointer) }
}
