package org.kodein.memory.io

public class VerificationWriteable(private val from: Readable): Writeable {
    public class DiffException(public val position: Int) : Exception("Difference at byte $position.")

    override val position: Int get() = from.position

    override fun requestCanWrite(needed: Int): Unit = from.requestCanRead(needed)

    private inline fun check(assertion: () -> Boolean) {
        val currentPosition = from.position
        val success = try {
            assertion()
        } catch (_: IOException) {
            false
        }
        if (!success) throw DiffException(currentPosition)
    }

    override fun writeByte(value: Byte): Unit = check { from.readByte() == value }

    override fun writeShort(value: Short): Unit = check { from.readShort() == value }

    override fun writeInt(value: Int): Unit = check { from.readInt() == value }

    override fun writeLong(value: Long): Unit = check { from.readLong() == value }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(src.size - srcOffset >= length) { "Failed: src.size - srcOffset >= length (${src.size} - $srcOffset >= $length)" }
        check {
            src.forEach {
                if (from.readByte() != it) return@check false
            }
            true
        }
    }

    override fun writeBytes(src: ReadMemory) {
        check {
            repeat(src.size) {
                if (from.readByte() != src[it]) return@check false
            }
            true
        }
    }

    override fun writeBytes(src: Readable, length: Int) {
        check {
            repeat(length) {
                if (from.readByte() != src.readByte()) return@check false
            }
            true
        }
    }

    override fun flush() {}
}

public inline fun verify(from: Readable, block: Writeable.() -> Unit): Boolean {
    try {
        VerificationWriteable(from).block()
        return true
    } catch (_: VerificationWriteable.DiffException) {
        return false
    }
}
