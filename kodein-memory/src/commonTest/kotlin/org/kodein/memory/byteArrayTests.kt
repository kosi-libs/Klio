package org.kodein.memory

class ByteArrayKBufferTests : AbstractKBufferTests() {
    override fun alloc(size: Int): Allocation = Allocation.array(size)
}

class ByteArraySliceBuilderTests : AbstractSliceBuilderTest() {
    override fun sliceBuilder(size: Int): SliceBuilder = SliceBuilder(size, Allocation.Allocations::array)
}
