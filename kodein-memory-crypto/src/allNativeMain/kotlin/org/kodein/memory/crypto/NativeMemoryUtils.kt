package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*


internal fun MemScope.toReadOnlyCPointerMemory(memory: ReadMemory): CPointerMemory = when (val m = memory.internalMemory()) {
    is CPointerMemory -> m
    is ByteArrayMemory -> CPointerMemory(m.array.refTo(m.offset).getPointer(this), m.size)
    else -> {
        val copy = Allocation.nativeCopy(memory)
        defer {
            copy.fill(0)
            copy.close()
        }
        copy.memory
    }
}

internal fun MemScope.toReadWriteCPointerMemory(memory: Memory): CPointerMemory = when (val m = memory.internalMemory()) {
    is CPointerMemory -> m
    is ByteArrayMemory -> {
        val cpm = CPointerMemory(m.array.refTo(m.offset).getPointer(this), m.size)
        cpm
    }
    else -> {
        val copy = Allocation.nativeCopy(memory)
        defer {
            memory.putBytes(0, copy)
            copy.fill(0)
            copy.close()
        }
        copy.memory
    }
}

internal inline fun Writeable.writeNative(maxOutputLength: Int, crossinline write: (CPointerMemory) -> Int) {
    memScoped {
        if (this@writeNative is MemoryWriteable) {
            writeMemory {
                val output = toReadWriteCPointerMemory(it.internalMemory())
                write(output)
            }
        } else {
            val buffer = Allocation.native(maxOutputLength)
            defer { buffer.close() }
            val count = write(buffer.memory)
            writeBytes(buffer.slice(0, count))
        }
    }
}
