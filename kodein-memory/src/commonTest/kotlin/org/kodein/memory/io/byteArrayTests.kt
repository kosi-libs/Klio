package org.kodein.memory.io

class ByteArrayKBufferTests : AbstractKBufferTests() {
    override fun alloc(size: Int): Allocation = KBuffer.array(size).asManagedAllocation()
}

class ByteArraySliceBuilderTests : AbstractSliceBuilderTests() {
    override fun sliceBuilder(size: Int): SliceBuilder = SliceBuilder.array(size)
}

class ByteArrayExpandableBufferTests : AbstractExpandableBufferTests() {
    override fun alloc(size:Int): Allocation = KBuffer.array(size).asManagedAllocation()
}
