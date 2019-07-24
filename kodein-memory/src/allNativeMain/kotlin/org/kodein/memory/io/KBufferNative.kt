package org.kodein.memory.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer

fun KBuffer.Companion.wrap(pointer: CPointer<ByteVar>, size: Int) = CPointerKBuffer(pointer, size)
