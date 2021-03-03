package org.kodein.memory.io

public class VerificationWriteable(private val from: Readable): Writeable {
    public class DiffException(public val position: Int) : Exception("Difference at byte $position.")

    override val position: Int get() = from.position

    override fun requireCanWrite(needed: Int): Unit = from.requireCanRead(needed)

    private inline fun check(assertion: () -> Boolean) {
        val currentPosition = from.position
        val success = try {
            assertion()
        } catch (_: OutOfMemoryException) {
            false
        }
        if (!success) throw DiffException(currentPosition)
    }

    override fun putByte(value: Byte): Unit = check { from.readByte() == value }

    override fun putChar(value: Char): Unit = check { from.readChar() == value }

    override fun putShort(value: Short): Unit = check { from.readShort() == value }

    override fun putInt(value: Int): Unit = check { from.readInt() == value }

    override fun putLong(value: Long): Unit = check { from.readLong() == value }

    override fun putFloat(value: Float): Unit = check { from.readFloat() == value }

    override fun putDouble(value: Double): Unit = check { from.readDouble() == value }

    override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(src.size - srcOffset >= length) { "Failed: src.size - srcOffset >= length (${src.size} - $srcOffset >= $length)" }
        check {
            src.forEach {
                if (from.readByte() != it) return@check false
            }
            true
        }
    }

    override fun putMemoryBytes(src: ReadMemory, srcOffset: Int, length: Int) {
        check {
            repeat(length) {
                if (from.readByte() != src[srcOffset + it]) return@check false
            }
            true
        }
    }

    override fun putReadableBytes(src: Readable, length: Int) {
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
