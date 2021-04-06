package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView


public fun Memory.Companion.wrap(data: DataView): ArrayBufferMemory = ArrayBufferMemory(data)
public fun Memory.Companion.wrap(buffer: ArrayBuffer, offset: Int, size: Int): ArrayBufferMemory = wrap(DataView(buffer, offset, size))
public fun Memory.Companion.wrap(buffer: ArrayBuffer): ArrayBufferMemory = wrap(buffer, 0, buffer.byteLength)

public fun DataView.asMemory(): ArrayBufferMemory = Memory.wrap(this)
public fun ArrayBuffer.asMemory(offset: Int, size: Int): ArrayBufferMemory = Memory.wrap(this, offset, size)
public fun ArrayBuffer.asMemory(): ArrayBufferMemory = Memory.wrap(this)
