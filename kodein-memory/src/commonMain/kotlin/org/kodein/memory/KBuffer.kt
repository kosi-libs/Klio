package org.kodein.memory

interface KBuffer : WriteBuffer, ReadBuffer {

    val capacity: Int

    override var limit: Int

    fun clear()
    fun flip()
    fun rewind()

    override fun duplicate(): KBuffer
    override fun slice(): KBuffer
    override fun view(index: Int, length: Int): KBuffer

    override fun internalBuffer(): KBuffer

    companion object
}

fun KBuffer.limitHere() {
    limit = position
}

fun KBuffer.Companion.wrap(array: ByteArray) = ByteArrayKBuffer(array)
fun KBuffer.Companion.array(capacity: Int) = ByteArrayKBuffer(ByteArray(capacity))
