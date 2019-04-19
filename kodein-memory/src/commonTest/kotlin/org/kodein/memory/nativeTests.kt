package org.kodein.memory

class NativeKBUfferTests : AbstractKBufferTests() {
    override fun alloc(size:Int): Allocation = Allocation.native(size)
}

class NativeViewsTests : AbstractViewsTest() {
    override fun viewMaker(size: Int): SliceBuilder = SliceBuilder(size, Allocation.Allocations::native)
}
