package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView


public actual fun Allocation.Companion.native(size: Int): Allocation =
    DataViewMemory(DataView(ArrayBuffer(size))).asManagedAllocation()
