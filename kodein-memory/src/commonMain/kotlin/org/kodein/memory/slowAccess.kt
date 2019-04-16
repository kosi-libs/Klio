package org.kodein.memory

internal inline fun slowStoreShort(value: Short, set: (Int, Byte) -> Unit) {
    val intValue = value.toInt()
    set(0, (intValue ushr 0x08 and 0xFF).toByte())
    set(1, (intValue ushr 0x00 and 0xFF).toByte())
}

internal inline fun slowStoreInt(value: Int, set: (Int, Byte) -> Unit) {
    set(0, (value ushr 0x18 and 0xFF).toByte())
    set(1, (value ushr 0x10 and 0xFF).toByte())
    set(2, (value ushr 0x08 and 0xFF).toByte())
    set(3, (value ushr 0x00 and 0xFF).toByte())
}

internal inline fun slowStoreLong(value: Long, set: (Int, Byte) -> Unit) {
    set(0, ((value ushr 0x38) and 0xFF).toByte())
    set(1, ((value ushr 0x30) and 0xFF).toByte())
    set(2, ((value ushr 0x28) and 0xFF).toByte())
    set(3, ((value ushr 0x20) and 0xFF).toByte())
    set(4, ((value ushr 0x18) and 0xFF).toByte())
    set(5, ((value ushr 0x10) and 0xFF).toByte())
    set(6, ((value ushr 0x08) and 0xFF).toByte())
    set(7, ((value ushr 0x00) and 0xFF).toByte())
}

internal inline fun slowLoadShort(get: (Int) -> Byte): Short {
    return (
            (get(0).toInt() and 0xFF shl 0x08) or
            (get(1).toInt() and 0xFF shl 0x00)
    ).toShort()
}

internal inline fun slowLoadInt(get: (Int) -> Byte): Int {
    return (
            (get(0).toInt() and 0xFF shl 0x18) or
            (get(1).toInt() and 0xFF shl 0x10) or
            (get(2).toInt() and 0xFF shl 0x08) or
            (get(3).toInt() and 0xFF shl 0x00)
    )
}

internal inline fun slowLoadLong(get: (Int) -> Byte): Long {
    return (
            (get(0).toLong() and 0xFF shl 0x38) or
            (get(1).toLong() and 0xFF shl 0x30) or
            (get(2).toLong() and 0xFF shl 0x28) or
            (get(3).toLong() and 0xFF shl 0x20) or
            (get(4).toLong() and 0xFF shl 0x18) or
            (get(5).toLong() and 0xFF shl 0x10) or
            (get(6).toLong() and 0xFF shl 0x08) or
            (get(7).toLong() and 0xFF shl 0x00)
    )
}
