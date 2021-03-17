package org.kodein.memory.io

class ByteArrayMemoryTests : AbstractMemoryTests() {
    override fun alloc(size: Int): Allocation = Memory.array(size).asManagedAllocation()
}

class ByteArraySliceBuilderTests : AbstractSliceBuilderTests() {
    override fun sliceBuilder(size: Int): SliceBuilder = SliceBuilder.array(size)
}

class ByteArrayReusableTests : AbstractReusableTests() {
    override fun alloc(size:Int): Allocation = Memory.array(size).asManagedAllocation()
}
