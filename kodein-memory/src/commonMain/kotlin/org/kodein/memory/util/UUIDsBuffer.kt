package org.kodein.memory.util

import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.Readable
import org.kodein.memory.io.Writeable
import org.kodein.memory.io.array
import kotlin.math.min

fun Writeable.putUUID(uuid: UUID) {
    putLong(uuid.mostSignificantBits)
    putLong(uuid.leastSignificantBits)
}

fun Readable.readUUID(): UUID {
    val msb = readLong()
    val lsb = readLong()
    return UUID(msb, lsb)
}

fun UUID.Companion.from14Bytes(src: Readable): UUID {
    val remaining = src.remaining
    val fullSrc = if (remaining < 14) KBuffer.array(14) { putBytes(src) ; skip(14 - remaining) } else src
    val data = KBuffer.array(16) {
        putBytes(fullSrc, 6)
        put(0xC0.toByte())
        putBytes(fullSrc, 1)
        put(0x80.toByte())
        putBytes(fullSrc, 7)
    }
    return UUID(data.readLong(), data.readLong())
}

fun UUID.write14Bytes(dst: Writeable, len: Int = 14) {
    require(len in 1..14) { "Bad length ($len)" }
    var remaining = len

    val buffer = KBuffer.array(16) {
        putLong(mostSignificantBits)
        putLong(leastSignificantBits)
    }

    val count = min(remaining, 6)

    dst.putBytes(buffer, count)
    remaining -= count
    if (remaining <= 0) return

    buffer.skip(1)

    dst.putBytes(buffer, 1)
    if (--remaining <= 0) return

    buffer.skip(1)

    dst.putBytes(buffer, min(remaining, 7))
}
