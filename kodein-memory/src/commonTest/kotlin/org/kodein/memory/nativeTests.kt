package org.kodein.memory

class NativeKBUfferTests : AbstractKBufferTests() {
    override fun alloc(size:Int): Allocation = allocNativeKBuffer(size)
}

class NativeViewsTests : AbstractViewsTest() {
    override fun viewMaker(size: Int): SliceBuilder = SliceBuilder(size, ::allocNativeKBuffer)
}
