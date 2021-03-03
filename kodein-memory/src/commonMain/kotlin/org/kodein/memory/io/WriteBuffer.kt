package org.kodein.memory.io

public interface WriteBuffer : ResettableWriteable, WriteMemory {

    public val remaining: Int

    public fun sliceHere(): WriteBuffer = sliceHere(remaining)
    public fun sliceHere(length: Int): WriteBuffer

}
