package org.kodein.memory.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer

public fun KBuffer.Companion.wrap(pointer: CPointer<ByteVar>, size: Int): CPointerKBuffer = CPointerKBuffer(pointer, size)
