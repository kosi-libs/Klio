package org.kodein.memory.io

interface WriteBuffer : Writeable, WriteMemory {

    var position: Int

}
