package org.kodein.memory.io

class NativeKBufferTests : AbstractKBufferTests() {
    override fun alloc(size:Int): Allocation = Allocation.native(size)
}

class NativeSliceBuilderTests : AbstractSliceBuilderTest() {
    override fun sliceBuilder(size: Int): SliceBuilder = SliceBuilder(size, Allocation.Allocations::native)
}
