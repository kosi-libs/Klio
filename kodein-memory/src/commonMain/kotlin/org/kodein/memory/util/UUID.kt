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
import kotlin.native.concurrent.ThreadLocal
import kotlin.random.Random


@Serializable(with = UUID.KXStringSerializer::class)
public class UUID(public val mostSignificantBits: Long, public val leastSignificantBits: Long) : Comparable<UUID> {

    public fun version(): Int =
            (mostSignificantBits shr 12 and 0xFL).toInt()

    public fun variant(): Int =
            (leastSignificantBits.ushr((64L - leastSignificantBits.ushr(62)).toInt()) and (leastSignificantBits shr 63)).toInt()

    public fun gregorianTimestamp(): Long =
            if (version() != 1) throw UnsupportedOperationException("Not a time-based UUID")
            else ((mostSignificantBits and 0xFFFL) shl 48) or (((mostSignificantBits shr 16) and 0xFFFFL) shl 32) or mostSignificantBits.ushr(32)

    public fun unixTimestamp(): Long = timestampGregorianToUnix(gregorianTimestamp())

    public fun clockSequence(): Int =
            if (version() != 1) throw UnsupportedOperationException("Not a time-based UUID")
            else ((leastSignificantBits and 0x3FFF000000000000L).ushr(48)).toInt()

    public fun node(): Long =
            if (version() != 1) throw UnsupportedOperationException("Not a time-based UUID")
            else leastSignificantBits and 0xFFFFFFFFFFFFL

    override fun toString(): String {
        val buf = CharArray(36)
        formatUnsignedLong(leastSignificantBits, 4, buf, 24, 12)
        formatUnsignedLong(leastSignificantBits.ushr(48), 4, buf, 19, 4)
        formatUnsignedLong(mostSignificantBits, 4, buf, 14, 4)
        formatUnsignedLong(mostSignificantBits.ushr(16), 4, buf, 9, 4)
        formatUnsignedLong(mostSignificantBits.ushr(32), 4, buf, 0, 8)
        buf[23] = 45.toChar()
        buf[18] = 45.toChar()
        buf[13] = 45.toChar()
        buf[8] = 45.toChar()
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

    @ThreadLocal
    public companion object {
        private val digits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
        private const val MIN_CLOCK_SEQ_AND_NODE = -0x7F7F7F7F7F7F7F80L
        private const val MAX_CLOCK_SEQ_AND_NODE =  0x7F7F7F7F7F7F7F7FL
        private const val START_EPOCH = -12219292800000L // 15 oct 1582 00:00:00.000
        private val MIN_UNIX_TIMESTAMP = timestampGregorianToUnix(0)
        private val MAX_UNIX_TIMESTAMP = timestampGregorianToUnix(0xFFFFFFFFFFFFFFFL)

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

        public fun randomUUID(random: Random = Random.Default): UUID {
            val data = Memory.array(16)// { random.nextBytes(this, 16) }
            random.nextBytes(data)
            data[6] = (data[6].toInt() and 0x0F).toByte()
            data[6] = (data[6].toInt() or 0x40).toByte()
            data[8] = (data[8].toInt() and 0x3F).toByte()
            data[8] = (data[8].toInt() or 0x80).toByte()
            return UUID(data.getLong(0), data.getLong(8))
        }

        public fun fromString(name: String): UUID {
            val len = name.length
            require(len <= 36) { "UUID string too large" }
            val dash1 = name.indexOf(45.toChar(), 0)
            val dash2 = name.indexOf(45.toChar(), dash1 + 1)
            val dash3 = name.indexOf(45.toChar(), dash2 + 1)
            val dash4 = name.indexOf(45.toChar(), dash3 + 1)
            val dash5 = name.indexOf(45.toChar(), dash4 + 1)
            if (dash4 >= 0 && dash5 < 0) {
                var mostSigBits = name.substring(0, dash1).toLong(16) and 4294967295L
                mostSigBits = mostSigBits shl 16
                mostSigBits = mostSigBits or (name.substring(dash1 + 1, dash2).toLong(16) and 65535L)
                mostSigBits = mostSigBits shl 16
                mostSigBits = mostSigBits or (name.substring(dash2 + 1, dash3).toLong(16) and 65535L)
                var leastSigBits = name.substring(dash3 + 1, dash4).toLong(16) and 65535L
                leastSigBits = leastSigBits shl 48
                leastSigBits = leastSigBits or (name.substring(dash4 + 1, len).toLong(16) and 281474976710655L)
                return UUID(mostSigBits, leastSigBits)
            } else {
                throw IllegalArgumentException("Invalid UUID string: $name")
            }
        }

        private fun makeMsb(timestamp: Long): Long {
            var msb = 0L
            msb = msb or (0x00000000FFFFFFFFL and timestamp shl 32)
            msb = msb or (0x0000FFFF00000000L and timestamp).ushr(16)
            msb = msb or (0x0FFF000000000000L and timestamp).ushr(48)
            msb = msb or  0x0000000000001000L // sets the version to 1.
            return msb
        }

        private fun timestampUnixToGregorian(unixTimestampMillis: Long): Long {
            return (unixTimestampMillis - START_EPOCH) * 10000
        }

        private fun timestampGregorianToUnix(gregorianTimestampNano: Long): Long {
            return (gregorianTimestampNano / 10000) + START_EPOCH
        }

        public fun startOf(unixTimestampMillis: Long): UUID {
            val gregorianTimestamp = timestampUnixToGregorian(unixTimestampMillis)
            return UUID(makeMsb(gregorianTimestamp), MIN_CLOCK_SEQ_AND_NODE)
        }

        public fun endOf(unixTimestampMillis: Long): UUID {
            val gregorianTimestamp = timestampUnixToGregorian(unixTimestampMillis + 1) - 1
            return UUID(makeMsb(gregorianTimestamp), MAX_CLOCK_SEQ_AND_NODE)
        }

        private fun makeLsb(clockSequence: Long, node: Long): Long {
            var lsb = 0L
            lsb = lsb or (clockSequence and 0x3FFFL).shl(48)
            lsb = lsb or Long.MIN_VALUE
            lsb = lsb or node
            return lsb
        }

        public fun timeUUID(unixTimestampMillis: Long = currentTimestampMillis(), clockSequence: Int = -1, node: Long = -1L): UUID {
            require(unixTimestampMillis in MIN_UNIX_TIMESTAMP..MAX_UNIX_TIMESTAMP) { "Bad timestamp (must be in $MIN_UNIX_TIMESTAMP..$MAX_UNIX_TIMESTAMP)" }
            val realClockSeq = if (clockSequence == -1) Random.nextLong(0x4000L) else clockSequence.toLong()
            require(realClockSeq in 0L..0x3FFFL) { "Bad clock sequence (must be in 0..0x3FFF)" }
            val realNode = if (node == -1L) Random.nextLong(0x1000000000000L) else node
            require(realNode in 0L..0xFFFFFFFFFFFFL) { "Bad node (must be in 0..0xFFFFFFFFFFFF)" }
            val gregorianTimestamp = timestampUnixToGregorian(unixTimestampMillis)
            return UUID(makeMsb(gregorianTimestamp), makeLsb(realClockSeq, realNode))
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

        override fun deserialize(decoder: Decoder): UUID {
            decoder.decodeStructure(descriptor) {
                var most: Long = 0
                var least: Long = 0

                repeat(2) {
                    when (decodeElementIndex(descriptor)) {
                        0 -> most = decodeLongElement(descriptor, 0)
                        1 -> least = decodeLongElement(descriptor, 1)
                    }
                }
                return UUID(most, least)
            }
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
