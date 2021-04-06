package org.kodein.memory.text

import org.kodein.memory.io.*
import org.kodein.memory.io.swapEndian


public abstract class Charset(public val name: String) {

    public abstract fun codePointOf(char: Char): Int
    public abstract fun charOf(codePoint: Int): Char

    public abstract fun isCodePointValid(codePoint: Int): Boolean
    public fun isValid(char: Char): Boolean = isCodePointValid(codePointOf(char))

    internal fun checkCodePoint(codePoint: Int) {
        if (!isCodePointValid(codePoint)) error("Code point 0x${codePoint.toString(radix = 16)} is not valid in $name")
    }

    public abstract fun sizeOfCodePoint(codePoint: Int): Int
    public fun sizeOf(char: Char): Int = sizeOfCodePoint(codePointOf(char))

    public abstract fun headerSize(): Int

    public abstract fun encodeCodePoint(codePoint: Int, dst: Writeable): Int
    public fun encode(char: Char, dst: Writeable): Int = encodeCodePoint(codePointOf(char), dst)

    public abstract fun decodeCodePoint(src: Readable): Int
    public fun decode(src: Readable): Char = charOf(decodeCodePoint(src))

    public abstract fun tryDecodeCodePoint(src: Readable): Int
    public fun tryDecode(src: Readable): Int {
        val codePoint = tryDecodeCodePoint(src)
        if (codePoint == -1) return -1
        return charOf(codePoint).toInt()
    }

    public object Type {
        public abstract class ByteCharset(name: String, private val max: Int) : Charset(name) {
            override fun codePointOf(char: Char): Int = char.toInt()
            override fun charOf(codePoint: Int): Char = codePoint.toChar()

            override fun isCodePointValid(codePoint: Int): Boolean = codePoint in 0..max

            override fun sizeOfCodePoint(codePoint: Int): Int {
                checkCodePoint(codePoint)
                return 1
            }

            override fun headerSize(): Int = 0

            override fun encodeCodePoint(codePoint: Int, dst: Writeable): Int {
                checkCodePoint(codePoint)
                dst.writeByte(codePoint.toByte())
                return 1
            }

            override fun decodeCodePoint(src: Readable): Int = src.readByte().toInt()

            override fun tryDecodeCodePoint(src: Readable): Int {
                return src.tryReadByte()
            }
        }

        public abstract class AbstractUTF16Charset(name: String) : Charset(name) {
            override fun codePointOf(char: Char): Int = char.toInt()
            override fun charOf(codePoint: Int): Char = codePoint.toChar()

            override fun isCodePointValid(codePoint: Int): Boolean = codePoint in 0..0x10FFFF

            override fun sizeOfCodePoint(codePoint: Int): Int = when {
                codePoint <= 0xFFFF -> 2
                codePoint <= 0x10FFFF -> 4
                else -> error("Code point 0x${codePoint.toString(radix = 16)} is not valid in Unicode.")
            }
        }

        public abstract class UTF16Charset(name: String) : AbstractUTF16Charset(name) {
            public abstract val byteOrderMark: Short

            override fun headerSize(): Int = 0

            internal abstract fun codeUnitToShort(codeUnit: Int): Short
            internal abstract fun shortToCodeUnit(short: Short): Int

            override fun encodeCodePoint(codePoint: Int, dst: Writeable): Int {
                checkCodePoint(codePoint)

                if (codePoint <= 0xFFFF) {
                    dst.writeShort(codeUnitToShort(codePoint))
                    return 2
                }

                // 110110xx xxxxxxxx 110111yy yyyyyyyy
                val code = codePoint - 0x10000
                dst.writeShort(codeUnitToShort(code shr 10 and 0x3FF or 0xD800))
                dst.writeShort(codeUnitToShort(code and 0x3FF or 0xDC00))

                return 4
            }

            private inline fun decodeCodePoint(firstShort: Short, nextShort: () -> Short): Int {
                val c1 = shortToCodeUnit(firstShort)

                return when (c1 shr 10 and 0x3F) {
                    0x36 -> { // [110110]xx xxxxxxxx 110111yy yyyyyyyy
                        val c2 = shortToCodeUnit(nextShort())
                        val code = (c1 and 0x3FF shl 10) or (c2 and 0x3FF)
                        code + 0x10000
                    }
                    0x37 -> error("Invalid UTF-16 code unit (expecting a first unit but got a second unit)")
                    else -> c1
                }
            }

            private inline fun decodeCodePoint(readShort: () -> Short): Int = decodeCodePoint(readShort(), readShort)

            override fun decodeCodePoint(src: Readable): Int = decodeCodePoint(src::readShort)

            internal fun decodeCodePoint(firstShort: Short, src: Readable): Int = decodeCodePoint(firstShort, src::readShort)

            override fun tryDecodeCodePoint(src: Readable): Int {
                val buffer = Memory.array(2)
                val read = src.tryReadBytes(buffer)
                if (read == -1) return -1
                if (read != 2) error("Invalid end of stream.")
                return tryDecodeCodePoint(buffer, buffer.getShort(0), src)
            }

            internal fun tryDecodeCodePoint(buffer: Memory, firstShort: Short, src: Readable): Int {
                return decodeCodePoint(firstShort) {
                    val read = src.tryReadBytes(buffer)
                    if (read != 2) error("Invalid end of stream.")
                    buffer.getShort(0)
                }
            }
        }
    }

    public object ASCII : Type.ByteCharset("ASCII", 0x7F)

    @Suppress("ClassName")
    public object ISO8859_1 : Type.ByteCharset("ISO-8859-1", 0xFF)

    @Suppress("ClassName")
    public object ISO8859_15 : Type.ByteCharset("ISO-8859-15", 0xFF) {
        override fun codePointOf(char: Char): Int = when (char) {
            '€' -> 0xA4
            'Š' -> 0xA6
            'š' -> 0xA8
            'Ž' -> 0xB4
            'ž' -> 0xB8
            'Œ' -> 0xBC
            'œ' -> 0xBD
            'Ÿ' -> 0xBE
            else -> char.toInt()
        }

        override fun charOf(codePoint: Int): Char = when (codePoint) {
            0xA4 -> '€'
            0xA6 -> 'Š'
            0xA8 -> 'š'
            0xB4 -> 'Ž'
            0xB8 -> 'ž'
            0xBC -> 'Œ'
            0xBD -> 'œ'
            0xBE -> 'Ÿ'
            else -> codePoint.toChar()
        }
    }

    public object UTF16BE : Type.UTF16Charset("UTF-16BE") {
        override val byteOrderMark: Short get() = 0xFEFF.toShort()
        override fun codeUnitToShort(codeUnit: Int): Short = codeUnit.toShort()
        override fun shortToCodeUnit(short: Short): Int = short.toInt()
    }

    public object UTF16LE : Type.UTF16Charset("UTF-16LE") {
        override val byteOrderMark: Short get() = 0xFFFE.toShort()
        override fun codeUnitToShort(codeUnit: Int): Short = swapEndian(codeUnit.toShort())
        override fun shortToCodeUnit(short: Short): Int = swapEndian(short).toInt()
    }

    public class UTF16(public val encoder: Type.UTF16Charset = UTF16BE) : Type.AbstractUTF16Charset(name) {
        private var hasWrittenMark = false
        private val buffer = Memory.array(2)

        override fun headerSize(): Int = 2

        override fun encodeCodePoint(codePoint: Int, dst: Writeable): Int {
            var count = 0
            if (!hasWrittenMark) {
                dst.writeShort(encoder.byteOrderMark)
                hasWrittenMark = true
                count += 2
            }
            count += encoder.encodeCodePoint(codePoint, dst)
            return count
        }

        private var decoder: Type.UTF16Charset? = null
        override fun decodeCodePoint(src: Readable): Int {
            if (decoder == null) {
                when (val mark = src.readShort()) {
                    UTF16BE.byteOrderMark -> decoder = UTF16BE
                    UTF16LE.byteOrderMark -> decoder = UTF16LE
                    else -> {
                        decoder = UTF16BE
                        return decoder!!.decodeCodePoint(mark, src)
                    }
                }
            }
            return decoder!!.decodeCodePoint(src)
        }

        override fun tryDecodeCodePoint(src: Readable): Int {
            if (decoder == null) {
                val read = src.tryReadBytes(buffer)
                if (read == -1) return -1
                if (read != 2) error("Invalid end of stream.")
                when (val mark = buffer.getShort(0)) {
                    UTF16BE.byteOrderMark -> decoder = UTF16BE
                    UTF16LE.byteOrderMark -> decoder = UTF16LE
                    else -> {
                        decoder = UTF16BE
                        return decoder!!.tryDecodeCodePoint(buffer, mark, src)
                    }
                }
            }
            return decoder!!.tryDecodeCodePoint(src)
        }

        public companion object {
            public val name: String = "UTF-16"
        }
    }

    public object UTF8 : Charset("UTF-8") {
        override fun codePointOf(char: Char): Int = char.toInt()
        override fun charOf(codePoint: Int): Char {
            if (codePoint > 0xFFFF) error("Code point 0x${codePoint.toString(radix = 16)} cannot be represented as a 2 bytes Char.")
            return codePoint.toChar()
        }

        override fun isCodePointValid(codePoint: Int): Boolean = codePoint <= 0x10FFFF

        override fun sizeOfCodePoint(codePoint: Int): Int {
            return when {
                codePoint <= 0x7F -> 1
                codePoint <= 0x7FF -> 2
                codePoint <= 0xFFFF -> 3
                codePoint <= 0x10FFFF -> 4
                else -> error("Code point 0x${codePoint.toString(radix = 16)} is not valid in Unicode.")
            }
        }

        override fun headerSize(): Int = 0

        override fun encodeCodePoint(codePoint: Int, dst: Writeable): Int {
            checkCodePoint(codePoint)

            if (codePoint <= 0x7F) { // 0xxxxxxx
                dst.writeByte(codePoint.toByte())
                return 1
            }

            val count = when {
                codePoint <= 0x7FF -> { // 110xxxxx 10xxxxxx
                    dst.writeByte((codePoint shr 6 and 0x1F or 0xC0).toByte())
                    2
                }
                codePoint <= 0xFFFF -> { // 1110xxxx 10xxxxxx 10xxxxxx
                    dst.writeByte((codePoint shr 12 and 0x0F or 0xE0).toByte())
                    dst.writeByte((codePoint shr 6 and 0x3F or 0x80).toByte())
                    3
                }
                codePoint <= 0x10FFFF -> { // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                    dst.writeByte((codePoint shr 18 and 0x07 or 0xF0).toByte())
                    dst.writeByte((codePoint shr 12 and 0x3F or 0x80).toByte())
                    dst.writeByte((codePoint shr 6 and 0x3F or 0x80).toByte())
                    4
                }
                else -> throw IllegalStateException("Illegal code point")
            }
            dst.writeByte((codePoint and 0x3F or 0x80).toByte())
            return count
        }

        private inline fun decodeCodePoint(firstByte: Byte, readByte: () -> Byte): Int {
            val c0 = firstByte.toInt() and 0xFF
            when (c0) {
                in 0x00..0x7F -> { // 0xxxxxxx
                    return c0
                }
                in 0x80..0xBF -> error("Invalid UTF-8 code unit (expecting a first unit but got a continuation unit)")
                in 0xC0..0xDF -> { // 110xxxxx 10xxxxxx
                    val c1 = readByte().toInt()
                    return (c0 and 0x1F shl 6) or (c1 and 0x3F)
                }
                in 0xE0..0xEF -> { // 1110xxxx  10xxxxxx  10xxxxxx
                    val c1 = readByte().toInt()
                    val c2 = readByte().toInt()
                    return (c0 and 0x0F shl 12) or (c1 and 0x3F shl 6) or (c2 and 0x3F)
                }
                in 0xF0..0xF7 -> { // 1110xxxx  10xxxxxx  10xxxxxx
                    val c1 = readByte().toInt()
                    val c2 = readByte().toInt()
                    val c3 = readByte().toInt()
                    return (c0 and 0x07 shl 18) or (c1 and 0x3F shl 12) or (c2 and 0x3F shl 6) or (c3 and 0x3F)
                }
                else -> throw IllegalStateException("Illegal UTF-8 code unit.")
            }
        }

        override fun decodeCodePoint(src: Readable): Int =
            decodeCodePoint(src.readByte()) { src.readByte() }

        override fun tryDecodeCodePoint(src: Readable): Int {
            val firstByte = src.tryReadByte()
            if (firstByte == -1) return -1

            return decodeCodePoint(firstByte.toByte()) {
                val b = src.tryReadByte()
                if (b == -1) error("Invalid end of stream.")
                b.toByte()
            }
        }
    }

    public companion object {
        public fun named(name: String): Charset =
            when (name) {
                UTF8.name -> UTF8
                UTF16.name -> UTF16(UTF16BE)
                UTF16BE.name -> UTF16BE
                UTF16LE.name -> UTF16LE
                ASCII.name -> ASCII
                ISO8859_1.name -> ISO8859_1
                else -> throw NoSuchElementException("Unknown charset $name")
            }
    }
}
