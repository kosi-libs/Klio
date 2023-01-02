package org.kodein.memory.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.kodein.memory.io.*
import kotlin.random.Random


@Serializable(with = UUID.KXStringSerializer::class)
public class UUID(public val mostSignificantBits: Long, public val leastSignificantBits: Long) : Comparable<UUID> {

    public fun version(): Int = ((mostSignificantBits shr 12) and 0xFL).toInt()

    public fun variant(): Int =
        when ((leastSignificantBits ushr 61).toInt()) {
            in 0b000..0b011 -> 0
            in 0b100..0b101 -> 1
            0b110 -> 2
            0b111 -> 3
            else -> error("Unknown variant")
        }

    override fun toString(): String {
        val buf = CharArray(36)
        formatUnsignedLong(leastSignificantBits, 4, buf, 24, 12)
        formatUnsignedLong(leastSignificantBits.ushr(48), 4, buf, 19, 4)
        formatUnsignedLong(mostSignificantBits, 4, buf, 14, 4)
        formatUnsignedLong(mostSignificantBits.ushr(16), 4, buf, 9, 4)
        formatUnsignedLong(mostSignificantBits.ushr(32), 4, buf, 0, 8)
        buf[23] = '-'
        buf[18] = '-'
        buf[13] = '-'
        buf[8] = '-'
        return buf.concatToString()
    }

    override fun compareTo(other: UUID): Int =
            when {
                this.mostSignificantBits < other.mostSignificantBits -> -1
                this.mostSignificantBits > other.mostSignificantBits -> 1
                this.leastSignificantBits < other.leastSignificantBits -> -1
                this.leastSignificantBits > other.leastSignificantBits -> 1
                else -> 0
            }

    override fun hashCode(): Int {
        val hilo = this.mostSignificantBits xor this.leastSignificantBits
        return (hilo shr 32).toInt() xor hilo.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UUID

        if (mostSignificantBits != other.mostSignificantBits) return false
        if (leastSignificantBits != other.leastSignificantBits) return false

        return true
    }

    public companion object {
        public val NIL: UUID = UUID(0, 0)

        private val digits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

        @OptIn(ExperimentalStdlibApi::class)
        private fun formatUnsignedLong(value: Long, shift: Int, buf: CharArray, offset: Int, len: Int) {
            var work = value
            var charPos = offset + len
            val radix = 1 shl shift
            val mask = radix - 1

            do {
                --charPos
                buf[charPos] = digits[work.toInt() and mask].code.toChar()
                work = work ushr shift
            } while (charPos > offset)
        }

        public fun fromString(name: String): UUID {
            val len = name.length
            require(len <= 36) { "UUID string too large" }
            val dash1 = name.indexOf('-', 0)
            val dash2 = name.indexOf('-', dash1 + 1)
            val dash3 = name.indexOf('-', dash2 + 1)
            val dash4 = name.indexOf('-', dash3 + 1)
            val dash5 = name.indexOf('-', dash4 + 1)
            if (dash4 >= 0 && dash5 < 0) {
                var msb = name.substring(0, dash1).toLong(16) and 0xFFFFFFFFL
                msb = msb shl 16
                msb = msb or (name.substring(dash1 + 1, dash2).toLong(16) and 0xFFFFL)
                msb = msb shl 16
                msb = msb or (name.substring(dash2 + 1, dash3).toLong(16) and 0xFFFFL)
                var lsb = name.substring(dash3 + 1, dash4).toLong(16) and 0xFFFFL
                lsb = lsb shl 48
                lsb = lsb or (name.substring(dash4 + 1, len).toLong(16) and 0xFFFFFFFFFFFFL)
                return UUID(msb, lsb)
            } else {
                throw IllegalArgumentException("Invalid UUID string: $name")
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    public object KXStringSerializer : KSerializer<UUID> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): UUID = fromString(decoder.decodeString())

    }

    @OptIn(ExperimentalSerializationApi::class)
    public object KXBinarySerializer : KSerializer<UUID> {

        override val descriptor: SerialDescriptor = listSerialDescriptor(PrimitiveSerialDescriptor("bits", PrimitiveKind.LONG))

        override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeStructure(descriptor) {
                encodeLongElement(descriptor, 0, value.mostSignificantBits)
                encodeLongElement(descriptor, 1, value.leastSignificantBits)
            }
        }

        override fun deserialize(decoder: Decoder): UUID = decoder.decodeStructure(descriptor) {
            var most: Long = 0
            var least: Long = 0

            repeat(2) {
                when (decodeElementIndex(descriptor)) {
                    0 -> most = decodeLongElement(descriptor, 0)
                    1 -> least = decodeLongElement(descriptor, 1)
                }
            }
            UUID(most, least)
        }
    }
}


public fun Writeable.writeUUID(uuid: UUID) {
    writeLong(uuid.mostSignificantBits)
    writeLong(uuid.leastSignificantBits)
}

public fun Memory.putUUID(index: Int, uuid: UUID) {
    putLong(index, uuid.mostSignificantBits)
    putLong(index + 8, uuid.leastSignificantBits)
}

public fun Readable.readUUID(): UUID {
    val msb = readLong()
    val lsb = readLong()
    return UUID(msb, lsb)
}

public fun ReadMemory.getUUID(index: Int): UUID {
    val msb = getLong(index)
    val lsb = getLong(index + 8)
    return UUID(msb, lsb)
}


/* Time based UUIDs (version 1) */

private object TimeUUIDs {
    const val MIN_CLOCK_SEQ_AND_NODE = -0x7F7F7F7F7F7F7F80L
    const val MAX_CLOCK_SEQ_AND_NODE =  0x7F7F7F7F7F7F7F7FL
    const val START_EPOCH = -12219292800000L // 15 oct 1582 00:00:00.000
    val MIN_UNIX_TIMESTAMP = timestampGregorianToUnix(0)
    val MAX_UNIX_TIMESTAMP = timestampGregorianToUnix(0xFFFFFFFFFFFFFFFL)

    fun timestampGregorianToUnix(gregorianTimestampNano: Long): Long {
        return (gregorianTimestampNano / 10000) + START_EPOCH
    }

    fun timestampUnixToGregorian(unixTimestampMillis: Long): Long {
        return (unixTimestampMillis - START_EPOCH) * 10000
    }

    fun makeMsb(timestamp: Long): Long {
        var msb = 0L
        msb = msb or (0x00000000FFFFFFFFL and timestamp shl 32)
        msb = msb or (0x0000FFFF00000000L and timestamp).ushr(16)
        msb = msb or (0x0FFF000000000000L and timestamp).ushr(48)
        msb = msb or  0x0000000000001000L // sets the version to 1.
        return msb
    }

    fun makeLsb(clockSequence: Long, node: Long): Long {
        var lsb = 0L
        lsb = lsb or (clockSequence and 0x3FFFL).shl(48)
        lsb = lsb or Long.MIN_VALUE
        lsb = lsb or node
        return lsb
    }
}

public fun UUID.Companion.startOf(unixTimestampMillis: Long): UUID {
    val gregorianTimestamp = TimeUUIDs.timestampUnixToGregorian(unixTimestampMillis)
    return UUID(TimeUUIDs.makeMsb(gregorianTimestamp), TimeUUIDs.MIN_CLOCK_SEQ_AND_NODE)
}

public fun UUID.Companion.endOf(unixTimestampMillis: Long): UUID {
    val gregorianTimestamp = TimeUUIDs.timestampUnixToGregorian(unixTimestampMillis + 1) - 1
    return UUID(TimeUUIDs.makeMsb(gregorianTimestamp), TimeUUIDs.MAX_CLOCK_SEQ_AND_NODE)
}

public fun UUID.gregorianTimestamp(): Long =
    if (version() != 1) throw UnsupportedOperationException("Not a time-based UUID")
    else ((mostSignificantBits and 0xFFFL) shl 48) or (((mostSignificantBits shr 16) and 0xFFFFL) shl 32) or mostSignificantBits.ushr(32)

public fun UUID.unixTimestamp(): Long = TimeUUIDs.timestampGregorianToUnix(gregorianTimestamp())

public fun UUID.clockSequence(): Int =
    if (version() != 1) throw UnsupportedOperationException("Not a time-based UUID")
    else ((leastSignificantBits and 0x3FFF000000000000L).ushr(48)).toInt()

public fun UUID.node(): Long =
    if (version() != 1) throw UnsupportedOperationException("Not a time-based UUID")
    else leastSignificantBits and 0xFFFFFFFFFFFFL

public fun UUID.Companion.timeUUID(unixTimestampMillis: Long = currentTimestampMillis(), clockSequence: Int = -1, node: Long = -1L): UUID {
    require(unixTimestampMillis in TimeUUIDs.MIN_UNIX_TIMESTAMP..TimeUUIDs.MAX_UNIX_TIMESTAMP) { "Bad timestamp (must be in ${TimeUUIDs.MIN_UNIX_TIMESTAMP}..${TimeUUIDs.MAX_UNIX_TIMESTAMP})" }
    val realClockSeq = if (clockSequence == -1) Random.nextLong(0x4000L) else clockSequence.toLong()
    require(realClockSeq in 0L..0x3FFFL) { "Bad clock sequence (must be in 0..0x3FFF)" }
    val realNode = if (node == -1L) Random.nextLong(0x1000000000000L) else node
    require(realNode in 0L..0xFFFFFFFFFFFFL) { "Bad node (must be in 0..0xFFFFFFFFFFFF)" }
    val gregorianTimestamp = TimeUUIDs.timestampUnixToGregorian(unixTimestampMillis)
    return UUID(TimeUUIDs.makeMsb(gregorianTimestamp), TimeUUIDs.makeLsb(realClockSeq, realNode))
}


/* Random UUIDs (version 4) */

public fun UUID.Companion.randomUUID(random: Random = Random.Default): UUID {
    val data = Memory.array(16)// { random.nextBytes(this, 16) }
    random.nextBytes(data)
    data[6] = (data[6].toInt() and 0x0F).toByte()
    data[6] = (data[6].toInt() or 0x40).toByte()
    data[8] = (data[8].toInt() and 0x3F).toByte()
    data[8] = (data[8].toInt() or 0x80).toByte()
    return UUID(data.getLong(0), data.getLong(8))
}


/* Raw parts UUIDs */

@OptIn(ExperimentalUnsignedTypes::class, kotlin.ExperimentalStdlibApi::class)
public fun UUID.Companion.fromRawLongs(high: ULong, low: ULong, version: Int, variant: Int = 1): UUID {
    require(version in 0..0xF) { "Version must be in 0..0xF" }

    require(high in 0uL..0xF_FF_FF_FF_FF_FF_FF_FF_uL) { "High must be in 0..0xFFFFFFFFFFFFFFF" }

    val (variantBitCount, variantCode) = when (variant) {
        0 -> 1 to 0b0
        1 -> 2 to 0b10
        2 -> 3 to 0b110
        3 -> 3 to 0b111
        else -> throw IllegalArgumentException("Variant must be in 0..3")
    }

    val maxLow = (1uL shl (64 - variantBitCount) - 1)
    require(low in 0uL..maxLow) { "Low with variant $variant must be in 0..0x${maxLow.toString(radix = 16).uppercase()}" }

    val msb = run {
        val l = (high shl 4) and 0xFFFFFFFFFFFF0000uL
        val r = high and 0xFFFuL
        val v = version.toULong() shl 12
        l or r or v
    }

    val lsb = run {
        val v = variantCode.toULong() shl (64 - variantBitCount)
        low or v
    }

    return UUID(msb.toLong(), lsb.toLong())
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun UUID.rawLongHigh(): ULong {
    val msb = mostSignificantBits.toULong()

    val l = (msb and 0xFFFFFFFFFFFF0000uL) shr 4
    val r = msb and 0xFFFuL
    return l or r
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun UUID.rawLongLow(): ULong {
    val lsb = leastSignificantBits.toULong()

    val bitCount = when (variant()) {
        0 -> 1
        1 -> 2
        2, 3 -> 3
        else -> error("Unknown variant")
    }

    val mask = (1uL shl (64 - bitCount)) - 1uL

    return lsb and mask
}


/* Raw bytes UUIDs */

public fun UUID.Companion.fromRawBytes(input: Memory, version: Int, variant: Int = 1): UUID {
    require(input.size == 15) { "Input must be exactly 15 bytes (not ${input.size})" }
    require(version in 0..0xF) { "Version must be in 0..0xF" }

    val variantCode = when (variant) {
        0 -> 0b0000
        1 -> 0b1000
        2 -> 0b1100
        3 -> 0b1110
        else -> throw IllegalArgumentException("Variant must be in 0..3")
    }

    val buffer = Memory.array(16) {
        writeBytes(input.slice(0, 6))
        writeByte(((version shl 4 and 0xF0) or (input[6].toInt() ushr 4 and 0x0F)).toByte())
        writeByte(((input[6].toInt() shl 4 and 0xF0) or (input[7].toInt() ushr 4 and 0x0F)).toByte())
        writeByte(((variantCode shl 4 and 0xF0) or (input[7].toInt() and 0x0F)).toByte())
        writeBytes(input.slice(8, 7))
    }

    return UUID(buffer.getLong(0), buffer.getLong(8))
}

public fun UUID.rawBytes(output: Writeable) {
    val buffer = Memory.array(16) {
        writeLong(mostSignificantBits)
        writeLong(leastSignificantBits)
    }

    output.writeBytes(buffer.slice(0, 6))
    output.writeByte(((buffer[6].toInt() shl 4 and 0xF0) or (buffer[7].toInt() ushr 4 and 0x0F)).toByte())
    output.writeByte(((buffer[7].toInt() shl 4 and 0xF0) or (buffer[8].toInt() and 0x0F)).toByte())
    output.writeBytes(buffer.slice(9, 7))
}

public fun UUID.rawBytes(): ByteArray = Memory.array(15) { rawBytes(this) } .array


/* Raw parts UUIDs */

@OptIn(ExperimentalUnsignedTypes::class, kotlin.ExperimentalStdlibApi::class)
public fun UUID.Companion.fromRawParts(part1: UInt, part2: UShort, part3: UShort, part4: UShort, part5: ULong, version: Int, variant: Int = 1): UUID {
    require(part3 in 0u..0xFFFu) { "Part3 must be in 0..0xFF" }

    val (variantBitCount, variantCode) = when (variant) {
        0 -> 1 to 0b0
        1 -> 2 to 0b10
        2 -> 3 to 0b110
        3 -> 3 to 0b111
        else -> throw IllegalArgumentException("Variant must be in 0..3")
    }

    val maxPart4 = (1u shl (16 - variantBitCount)) - 1u
    require(part4 in 0u..maxPart4) { "Part4 with variant $variant must be in 0..0x${maxPart4.toString(radix = 16).uppercase()}" }

    require(part5 in 0uL..0xFF_FF_FF_FF_FF_FF_uL) { "part5 must be in 0..0xFFFFFFFFFFFF" }

    val msb =
        (part1.toULong() shl 32) or
        (part2.toULong() shl 16) or
        (version.toULong() shl 12) or
        part3.toULong()

    val lsb =
        (variantCode.toULong() shl (64 - variantBitCount)) or
        (part4.toULong() shl 48) or
        part5

    return UUID(msb.toLong(), lsb.toLong())
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun UUID.Companion.fromRawParts(part1: UInt, part2: UShort, part3: UShort, part4: UShort, part5: Memory, version: Int, variant: Int = 1): UUID {
    require(part5.size == 6) { "Part5 must be exactly 6 bytes (not ${part5.size})" }

    val part5Long = Memory.array(8) { writeShort(0) ; writeBytes(part5) } .getULong(0)

    return fromRawParts(part1, part2, part3, part4, part5Long, version, variant)
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun UUID.part1(): UInt = (mostSignificantBits ushr 32 and 0xFF_FF_FF_FF).toUInt()

@OptIn(ExperimentalUnsignedTypes::class)
public fun UUID.part2(): UShort = (mostSignificantBits ushr 16 and 0xFF_FF).toUShort()

@OptIn(ExperimentalUnsignedTypes::class)
public fun UUID.part3(): UShort = (mostSignificantBits and 0x0F_FF).toUShort()

@OptIn(ExperimentalUnsignedTypes::class)
public fun UUID.part4(): UShort {
    val bitCount = when (variant()) {
        0 -> 1
        1 -> 2
        2, 3 -> 3
        else -> error("Unknown variant")
    }

    val mask = (1L shl (16 - bitCount)) - 1L

    return (leastSignificantBits ushr 48 and mask).toUShort()
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun UUID.part5Long(): ULong = (leastSignificantBits and 0xFF_FF_FF_FF_FF_FF).toULong()

public fun UUID.part5Bytes(output: Writeable) {
    val bytes = Memory.array(8) { writeLong(leastSignificantBits) } .sliceAt(2)
    output.writeBytes(bytes)
}

public fun UUID.part5Bytes(): ByteArray = Memory.array(6) { part5Bytes(this) } .array
