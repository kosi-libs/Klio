package org.kodein.memory.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer

public fun Memory.Companion.wrap(pointer: CPointer<ByteVar>, size: Int): CPointerMemory = CPointerMemory(pointer, size)
