package org.kodein.memory.io

class VerificationWriteable(private val from: Readable): Writeable {
    class DiffException : Exception()

    override val remaining: Int get() = from.remaining

    private inline fun check(count: Int, assertion: () -> Boolean) {
        if (remaining >= count && !assertion()) {
            from.skip(from.remaining)
            throw DiffException()
        }
    }

    override fun put(value: Byte) = check(Byte.SIZE_BYTES) { from.read() == value }

    override fun putChar(value: Char) = check(Char.SIZE_BYTES) { from.readChar() == value }

    override fun putShort(value: Short) = check(Short.SIZE_BYTES) { from.readShort() == value }

    override fun putInt(value: Int) = check(Int.SIZE_BYTES) { from.readInt() == value }

    override fun putLong(value: Long) = check(Long.SIZE_BYTES) { from.readLong() == value }

    override fun putFloat(value: Float) = check(Int.SIZE_BYTES) { from.readFloat() == value }

    override fun putDouble(value: Double) = check(Long.SIZE_BYTES) { from.readDouble() == value }

    override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(src.size - srcOffset >= length) { "Failed: src.size - srcOffset >= length (${src.size} - $srcOffset >= $length)" }
        check(length) {
            src.forEach {
                if (from.read() != it) return@check false
            }
            true
        }
    }

    override fun putBytes(src: Readable, length: Int) {
        require(src.remaining >= length) { "Failed: src.remaining >= length (${src.remaining} >= $length)" }
        check(length) {
            repeat(length) {
                if (from.read() != src.read()) return@check false
            }
            true
        }
    }

}

inline fun verify(from: Readable, block: Writeable.() -> Unit): Boolean {
    try {
        VerificationWriteable(from).block()
        return true
    } catch (_: VerificationWriteable.DiffException) {
        return false
    }
}
