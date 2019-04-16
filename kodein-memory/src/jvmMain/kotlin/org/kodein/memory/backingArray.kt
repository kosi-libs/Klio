package org.kodein.memory

class BackingArray(val array: ByteArray, val offset: Int)

fun KBuffer.backingArray(): BackingArray? =
        when (this) {
            is ByteArrayKBuffer -> BackingArray(array, offset)
            is JvmNioKBuffer -> if (byteBuffer.hasArray()) BackingArray(byteBuffer.array(), byteBuffer.arrayOffset()) else null
            else -> null
        }
