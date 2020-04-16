package org.kodein.memory.util

import org.kodein.memory.io.*
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
    val array = ByteArray(16)
    src.receive(array, 0, 6)
    array[6] = 0xC0.toByte()
    src.receive(array, 7, 1)
    array[8] = 0x80.toByte()
    src.receive(array, 9, 7)

    val data = KBuffer.wrap(array)
    return UUID(data.readLong(), data.readLong())
}

fun UUID.write14Bytes(dst: Writeable, len: Int = 14) {
    require(len in 1..14) { "Bad length ($len)" }
    var available = len

    val buffer = KBuffer.array(16) {
        putLong(mostSignificantBits)
        putLong(leastSignificantBits)
    }

    val count = min(available, 6)

    dst.putBytes(buffer, count)
    available -= count
    if (available <= 0) return

    buffer.skip(1)

    dst.putBytes(buffer, 1)
    if (--available <= 0) return

    buffer.skip(1)

    dst.putBytes(buffer, min(available, 7))
}
