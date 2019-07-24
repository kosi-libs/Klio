package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer


fun KBuffer.Companion.wrap(buffer: ArrayBuffer, offset: Int = 0, capacity: Int = buffer.byteLength - offset) = TypedArrayKBuffer(buffer, offset, capacity)
