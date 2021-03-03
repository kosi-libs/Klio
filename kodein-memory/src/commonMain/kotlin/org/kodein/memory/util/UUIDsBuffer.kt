package org.kodein.memory.util

import org.kodein.memory.io.*
import kotlin.math.min

public fun Writeable.putUUID(uuid: UUID) {
    putLong(uuid.mostSignificantBits)
    putLong(uuid.leastSignificantBits)
}

public fun Readable.readUUID(): UUID {
    val msb = readLong()
    val lsb = readLong()
    return UUID(msb, lsb)
}

public fun UUID.Companion.from14Bytes(src: Readable): UUID {
    val array = ByteArray(16)
    src.receive(array, 0, 6)
    array[6] = 0xC0.toByte()
    src.receive(array, 7, 1)
    array[8] = 0x80.toByte()
    src.receive(array, 9, 7)

    val data = KBuffer.wrap(array)
    return UUID(data.readLong(), data.readLong())
}

public fun UUID.write14Bytes(dst: Writeable, len: Int = 14) {
    require(len in 1..14) { "Bad length ($len)" }
    var remaining = len

    val buffer = KBuffer.array(16) {
        putLong(mostSignificantBits)
        putLong(leastSignificantBits)
    }

    val count = min(remaining, 6)

    dst.putReadableBytes(buffer, count)
    remaining -= count
    if (remaining <= 0) return

    buffer.skip(1)

    dst.putReadableBytes(buffer, 1)
    if (--remaining <= 0) return

    buffer.skip(1)

    dst.putReadableBytes(buffer, min(remaining, 7))
}
