package org.kodein.memory

class ByteArrayKBufferTests : AbstractKBufferTests() {
    override fun alloc(size: Int): Allocation = HeapAllocation(allocArrayKBuffer(size))
}

class ByteArrayViewsTests : AbstractViewsTest() {
    override fun viewMaker(size: Int): SliceBuilder = SliceBuilder(size) { HeapAllocation(allocArrayKBuffer(it)) }
}
