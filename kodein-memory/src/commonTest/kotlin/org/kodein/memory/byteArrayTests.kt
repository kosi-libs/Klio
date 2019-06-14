package org.kodein.memory

class ByteArrayKBufferTests : AbstractKBufferTests() {
    override fun alloc(size: Int): Allocation = KBuffer.array(size).asManagedAllocation()
}

class ByteArraySliceBuilderTests : AbstractSliceBuilderTest() {
    override fun sliceBuilder(size: Int): SliceBuilder = SliceBuilder(size) { KBuffer.array(it).asManagedAllocation() }
}
