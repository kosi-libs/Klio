package org.kodein.memory

class ByteArrayKBufferTests : AbstractKBufferTests() {
    override fun alloc(size: Int): Allocation = Allocation.array(size)
}

class ByteArrayViewsTests : AbstractViewsTest() {
    override fun viewMaker(size: Int): SliceBuilder = SliceBuilder(size, Allocation.Allocations::array)
}
