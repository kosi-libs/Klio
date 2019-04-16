package org.kodein.memory

interface KBuffer : Writeable, Readable {

    val capacity: Int

    var position: Int

    var limit: Int

    fun clear()
    fun flip()
    fun rewind()

    fun mark()
    fun reset()

    fun duplicate(): KBuffer
    fun slice(): KBuffer
}
