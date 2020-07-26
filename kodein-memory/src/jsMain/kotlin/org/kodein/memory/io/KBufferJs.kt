package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer


public fun KBuffer.Companion.wrap(buffer: ArrayBuffer, offset: Int = 0, limit: Int = buffer.byteLength - offset): TypedArrayKBuffer = TypedArrayKBuffer(buffer).also {
    it.offset(offset)
    it.limit = limit
}
