package org.kodein.memory.io

class NativeMemoryTests : AbstractMemoryTests() {
    override fun alloc(size:Int): Allocation = Allocation.native(size)
}

class NativeSliceBuilderTests : AbstractSliceBuilderTests() {
    override fun sliceBuilder(size: Int): SliceBuilder = SliceBuilder.native(size)
}

class NativeReusableTests : AbstractReusableTests() {
    override fun alloc(size:Int): Allocation = Allocation.native(size)
}
