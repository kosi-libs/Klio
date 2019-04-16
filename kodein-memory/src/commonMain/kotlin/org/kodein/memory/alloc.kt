package org.kodein.memory

fun allocArrayKBuffer(capacity: Int): KBuffer = ByteArrayKBuffer(ByteArray(capacity))

interface Allocation : KBuffer, Closeable

internal class NativeAllocation(private val buffer: KBuffer, private val closeFun: () -> Unit) : Allocation, KBuffer by buffer {
    override fun close() = closeFun()
}

internal class HeapAllocation(private val buffer: KBuffer) : Allocation, KBuffer by buffer {
    override fun close() {}
}

expect fun allocNative(capacity: Int): Allocation
