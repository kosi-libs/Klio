package org.kodein.memory

interface Allocation : KBuffer, Closeable

internal class NativeAllocation(private val buffer: KBuffer, private val closeFun: () -> Unit) : Allocation, KBuffer by buffer {
    private var isClosed = false
    override fun close() {
        if (!isClosed) {
            isClosed = true
            closeFun()
        }
    }
}

internal class ManagedAllocation(private val buffer: KBuffer) : Allocation, KBuffer by buffer {
    override fun close() {}
}

fun allocArrayKBuffer(capacity: Int): Allocation = ManagedAllocation(ByteArrayKBuffer(ByteArray(capacity)))

expect fun allocNativeKBuffer(capacity: Int): Allocation
