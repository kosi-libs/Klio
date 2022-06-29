package org.kodein.memory.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap


public actual typealias PlatformNativeMemory = CPointerMemory

public actual fun Allocation.Companion.native(size: Int): PlatformNativeAllocation {
    val pointer = nativeHeap.allocArray<ByteVar>(size)
    return MemoryAllocation(CPointerMemory(pointer, size)) { nativeHeap.free(pointer) }
}
