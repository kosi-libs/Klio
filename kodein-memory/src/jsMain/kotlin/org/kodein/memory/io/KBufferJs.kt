package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer


fun KBuffer.Companion.wrap(buffer: ArrayBuffer, offset: Int = 0, limit: Int = buffer.byteLength - offset) = TypedArrayKBuffer(buffer).also {
    it.offset(offset)
    it.limit = limit
}
