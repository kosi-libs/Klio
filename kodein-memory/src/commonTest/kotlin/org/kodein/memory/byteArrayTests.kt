package org.kodein.memory

class ByteArrayKBufferTests : AbstractKBufferTests() {
    override fun alloc(size: Int): Allocation = ManagedAllocation(allocArrayKBuffer(size))
}

class ByteArrayViewsTests : AbstractViewsTest() {
    override fun viewMaker(size: Int): SliceBuilder = SliceBuilder(size, ::allocArrayKBuffer)
}
