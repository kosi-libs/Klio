package org.kodein.memory.io

import org.kodein.memory.text.Charset
import org.kodein.memory.text.putString
import org.kodein.memory.text.sizeOf
import kotlin.math.ceil

@UseExperimental(ExperimentalUnsignedTypes::class)
class TableBuilder internal constructor(private val writeable: Writeable) {

    private var index: Int = 0

    companion object {
        private fun Writeable.putHeader(name: String, type: Byte) {
            val nameByteSize = Charset.UTF8.sizeOf(name)
            if (nameByteSize > UByte.MAX_VALUE.toInt())
                throw IllegalArgumentException("Name size of element cannot be greater than ${UByte.MAX_VALUE}")

            putUByte(nameByteSize.toUByte())
            putString(name)
            put(type)
        }
    }

    fun byte(name: String, value: Byte): Int {
        writeable.putHeader(name, Table.TYPE_BYTE)
        writeable.put(value)
        return index++
    }

    fun char(name: String, value: Char): Int {
        writeable.putHeader(name, Table.TYPE_CHAR)
        writeable.putChar(value)
        return index++
    }

    fun short(name: String, value: Short): Int {
        writeable.putHeader(name, Table.TYPE_SHORT)
        writeable.putShort(value)
        return index++
    }

    fun int(name: String, value: Int): Int {
        writeable.putHeader(name, Table.TYPE_INT)
        writeable.putInt(value)
        return index++
    }

    fun long(name: String, value: Long): Int {
        writeable.putHeader(name, Table.TYPE_LONG)
        writeable.putLong(value)
        return index++
    }

    fun float(name: String, value: Float): Int {
        writeable.putHeader(name, Table.TYPE_FLOAT)
        writeable.putFloat(value)
        return index++
    }

    fun double(name: String, value: Double): Int {
        writeable.putHeader(name, Table.TYPE_DOUBLE)
        writeable.putDouble(value)
        return index++
    }

    fun string(name: String, value: String): Int {
        val byteSize = Charset.UTF8.sizeOf(value)
        if (byteSize > UShort.MAX_VALUE.toInt())
            throw IllegalArgumentException("Byte size of string cannot be greater than ${UShort.MAX_VALUE}")
        writeable.putHeader(name, Table.TYPE_STRING)
        writeable.putUShort(byteSize.toUShort())
        writeable.putString(value)
        return index++
    }

    fun booleans(name: String, value: BooleanArray): Int {
        writeable.putHeader(name, Table.TYPE_BOOLEANS)
        val byteSize = ceil(value.size.toDouble() / 8.0).toInt()
        if (byteSize > UShort.MAX_VALUE.toInt())
            throw IllegalArgumentException("There cannot be more booleans than ${UShort.MAX_VALUE.toInt() * 8} (seriously ?!?)")
        writeable.putUShort(byteSize.toUShort())

        for (arrayIndex in value.indices step 8) {
            val b0 = value.getOrElse(arrayIndex + 0) { false } .let { if (it) 1 else 0 }
            val b1 = value.getOrElse(arrayIndex + 1) { false } .let { if (it) 1 else 0 }
            val b2 = value.getOrElse(arrayIndex + 2) { false } .let { if (it) 1 else 0 }
            val b3 = value.getOrElse(arrayIndex + 3) { false } .let { if (it) 1 else 0 }
            val b4 = value.getOrElse(arrayIndex + 4) { false } .let { if (it) 1 else 0 }
            val b5 = value.getOrElse(arrayIndex + 5) { false } .let { if (it) 1 else 0 }
            val b6 = value.getOrElse(arrayIndex + 6) { false } .let { if (it) 1 else 0 }
            val b7 = value.getOrElse(arrayIndex + 7) { false } .let { if (it) 1 else 0 }

            val byte = (
                        (b0 shl 7)
                    or  (b1 shl 6)
                    or  (b2 shl 5)
                    or  (b3 shl 4)
                    or  (b4 shl 3)
                    or  (b5 shl 2)
                    or  (b6 shl 1)
                    or  (b7 shl 0)
            )

            writeable.put(byte.toByte())
        }

        return index++
    }

    fun bytes(name: String, src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset): Int {
        if (length > UShort.MAX_VALUE.toInt())
            throw IllegalArgumentException("ByteArray length cannot be greater than ${UShort.MAX_VALUE}")
        writeable.putHeader(name, Table.TYPE_BYTES)
        writeable.putUShort(length.toUShort())
        writeable.putBytes(src, srcOffset, length)
        return index++
    }

    fun bytes(name: String, src: Readable, length: Int = src.remaining): Int {
        if (length > UShort.MAX_VALUE.toInt())
            throw IllegalArgumentException("ByteArray length cannot be greater than ${UShort.MAX_VALUE}")
        writeable.putHeader(name, Table.TYPE_BYTES)
        writeable.putUShort(length.toUShort())
        writeable.putBytes(src, length)
        return index++
    }

//    fun bytes(name: String, builder: Writeable.() -> Unit)

//    fun table(name: String, builder: TableBuilder.() -> Unit)

//    fun copy(table: Table, filter: (String) -> Boolean = {true})
}

fun Writeable.writeTable(builder: TableBuilder.() -> Unit) {
    TableBuilder(this).builder()
}
