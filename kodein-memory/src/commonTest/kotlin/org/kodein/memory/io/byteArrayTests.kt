package org.kodein.memory.io

class ByteArrayMemoryTests : AbstractMemoryTests() {
    override fun alloc(size: Int): Allocation = Memory.array(size).asManagedAllocation()
}

class ByteArrayReusableTests : AbstractReusableTests() {
    override fun alloc(size:Int): Allocation = Memory.array(size).asManagedAllocation()
}
