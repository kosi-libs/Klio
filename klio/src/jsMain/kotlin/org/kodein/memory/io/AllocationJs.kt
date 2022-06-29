package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView


public actual typealias PlatformNativeMemory = ArrayBufferMemory

public actual fun Allocation.Companion.native(size: Int): PlatformNativeAllocation =
    MemoryAllocation(ArrayBufferMemory(DataView(ArrayBuffer(size)))) {}
