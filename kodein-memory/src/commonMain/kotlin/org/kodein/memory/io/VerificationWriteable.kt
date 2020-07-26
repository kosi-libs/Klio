package org.kodein.memory.io

public class VerificationWriteable(private val from: Readable): Writeable {
    public class DiffException : Exception()

    override val available: Int get() = from.available

    private inline fun check(count: Int, assertion: () -> Boolean) {
        if (available >= count && !assertion()) {
            throw DiffException()
        }
    }

    override fun putByte(value: Byte): Unit = check(Byte.SIZE_BYTES) { from.readByte() == value }

    override fun putChar(value: Char): Unit = check(Char.SIZE_BYTES) { from.readChar() == value }

    override fun putShort(value: Short): Unit = check(Short.SIZE_BYTES) { from.readShort() == value }

    override fun putInt(value: Int): Unit = check(Int.SIZE_BYTES) { from.readInt() == value }

    override fun putLong(value: Long): Unit = check(Long.SIZE_BYTES) { from.readLong() == value }

    override fun putFloat(value: Float): Unit = check(Int.SIZE_BYTES) { from.readFloat() == value }

    override fun putDouble(value: Double): Unit = check(Long.SIZE_BYTES) { from.readDouble() == value }

    override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(src.size - srcOffset >= length) { "Failed: src.size - srcOffset >= length (${src.size} - $srcOffset >= $length)" }
        check(length) {
            src.forEach {
                if (from.readByte() != it) return@check false
            }
            true
        }
    }

    override fun putBytes(src: Readable, length: Int) {
        check(length) {
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
