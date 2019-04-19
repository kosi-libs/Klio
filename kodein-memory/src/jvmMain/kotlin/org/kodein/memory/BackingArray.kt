package org.kodein.memory

class BackingArray(val array: ByteArray, val offset: Int = 0, val length: Int = array.size)

fun ReadBuffer.backingArray(): BackingArray? =
        when (val b = internalBuffer()) {
            is ByteArrayKBuffer -> BackingArray(b.array, b.offset, b.remaining)
            is JvmNioKBuffer -> if (b.byteBuffer.hasArray()) BackingArray(b.byteBuffer.array(), b.byteBuffer.arrayOffset(), b.byteBuffer.remaining()) else null
            else -> null
        }
