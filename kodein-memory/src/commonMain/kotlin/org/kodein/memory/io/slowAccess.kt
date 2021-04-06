package org.kodein.memory.io

public inline fun slowStoreShort(value: Short, set: (Int, Byte) -> Unit) {
    val intValue = value.toInt()
    set(0, (intValue ushr 0x08 and 0xFF).toByte())
    set(1, (intValue ushr 0x00 and 0xFF).toByte())
}

public inline fun slowStoreInt(value: Int, set: (Int, Byte) -> Unit) {
    set(0, (value ushr 0x18 and 0xFF).toByte())
    set(1, (value ushr 0x10 and 0xFF).toByte())
    set(2, (value ushr 0x08 and 0xFF).toByte())
    set(3, (value ushr 0x00 and 0xFF).toByte())
}

public inline fun slowStoreLong(value: Long, set: (Int, Byte) -> Unit) {
    set(0, ((value ushr 0x38) and 0xFF).toByte())
    set(1, ((value ushr 0x30) and 0xFF).toByte())
    set(2, ((value ushr 0x28) and 0xFF).toByte())
    set(3, ((value ushr 0x20) and 0xFF).toByte())
    set(4, ((value ushr 0x18) and 0xFF).toByte())
    set(5, ((value ushr 0x10) and 0xFF).toByte())
    set(6, ((value ushr 0x08) and 0xFF).toByte())
    set(7, ((value ushr 0x00) and 0xFF).toByte())
}

public inline fun slowLoadShort(get: (Int) -> Byte): Short {
    val b0 = get(0)
    val b1 = get(1)
    return (
            (b0.toInt() and 0xFF shl 0x08) or
            (b1.toInt() and 0xFF shl 0x00)
    ).toShort()
}

public inline fun slowLoadInt(get: (Int) -> Byte): Int {
    val b0 = get(0)
    val b1 = get(1)
    val b2 = get(2)
    val b3 = get(3)
    return (
            (b0.toInt() and 0xFF shl 0x18) or
            (b1.toInt() and 0xFF shl 0x10) or
            (b2.toInt() and 0xFF shl 0x08) or
            (b3.toInt() and 0xFF shl 0x00)
    )
}

public inline fun slowLoadLong(get: (Int) -> Byte): Long {
    val b0 = get(0)
    val b1 = get(1)
    val b2 = get(2)
    val b3 = get(3)
    val b4 = get(4)
    val b5 = get(5)
    val b6 = get(6)
    val b7 = get(7)
    return (
            (b0.toLong() and 0xFF shl 0x38) or
            (b1.toLong() and 0xFF shl 0x30) or
            (b2.toLong() and 0xFF shl 0x28) or
            (b3.toLong() and 0xFF shl 0x20) or
            (b4.toLong() and 0xFF shl 0x18) or
            (b5.toLong() and 0xFF shl 0x10) or
            (b6.toLong() and 0xFF shl 0x08) or
            (b7.toLong() and 0xFF shl 0x00)
    )
}
