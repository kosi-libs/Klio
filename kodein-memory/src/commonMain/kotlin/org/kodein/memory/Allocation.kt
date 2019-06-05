package org.kodein.memory

interface Allocation : KBuffer, Closeable {
    companion object Allocations
}

internal class NativeAllocation(private val buffer: KBuffer, private val closeFun: () -> Unit) : Allocation, KBuffer by buffer {
    private var isClosed = false
    override fun close() {
        if (!isClosed) {
            isClosed = true
            closeFun()
        }
    }

    override fun equals(other: Any?) = buffer.equals(other)
    override fun hashCode() = buffer.hashCode()
}

internal class ManagedAllocation(private val buffer: KBuffer) : Allocation, KBuffer by buffer {
    override fun close() {}

    override fun equals(other: Any?) = buffer == other
    override fun hashCode() = buffer.hashCode()
}

expect fun Allocation.Allocations.native(capacity: Int): Allocation

fun KBuffer.asManagedAllocation(): Allocation = ManagedAllocation(this)

inline fun Allocation.Allocations.native(capacity: Int, block: KBuffer.() -> Unit): Allocation {
    val alloc = Allocation.native(capacity)
    alloc.block()
    alloc.flip()
    return alloc
}
