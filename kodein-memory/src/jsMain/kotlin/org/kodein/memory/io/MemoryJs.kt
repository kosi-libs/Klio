package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView


public fun Memory.Companion.wrap(data: DataView): DataViewMemory = DataViewMemory(data)
public fun Memory.Companion.wrap(buffer: ArrayBuffer, offset: Int = 0, size: Int = buffer.byteLength - offset): DataViewMemory =
    wrap(DataView(buffer, offset, size))
