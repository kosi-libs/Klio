package org.kodein.memory.text

import org.kodein.memory.io.Readable
import org.kodein.memory.io.Writeable
import org.kodein.memory.io.slowLoadShort
import org.kodein.memory.io.swapEndian


public abstract class Charset(public val name: String) {

    public abstract fun sizeOf(char: Char): Int
    public abstract fun headerSize(): Int
    public abstract fun encode(char: Char, dst: Writeable): Int
    public abstract fun decode(src: Readable): Char
    public abstract fun tryDecode(src: Readable): Int

    public object Type {
        public abstract class FixedSizeCharset(name: String, public val bytesPerChar: Int) : Charset(name) {
            final override fun sizeOf(char: Char): Int = bytesPerChar
        }

        public abstract class ByteCharset(name: String, private val max: Int) : FixedSizeCharset(name, bytesPerChar = 1) {
            private fun check(char: Char) {
                val int = charToInt(char)
                if (int !in 0..max) error("Character '$char' (0x${int.toString(radix = 16)}) is not an $name character")
            }

            protected open fun charToInt(c: Char): Int = c.toInt()
            protected open fun byteToChar(b: Byte): Char = b.toChar()

            override fun headerSize(): Int = 0

            override fun encode(char: Char, dst: Writeable): Int {
                check(char)
                dst.writeByte(charToInt(char).toByte())
                return 1
            }
            override fun decode(src: Readable): Char = byteToChar(src.readByte())
            override fun tryDecode(src: Readable): Int {
                val b = src.tryReadByte()
                if (b == -1) return -1
                return byteToChar(b.toByte()).toInt()
            }

            public fun stringToBytes(src: String): ByteArray =
                ByteArray(src.length) {
                    val char = src[it]
                    check(char)
                    charToInt(char).toByte()
                }
            public fun bytesToString(src: ByteArray): String =
                CharArray(src.size) { byteToChar(src[it]) }.concatToString()
        }

        public abstract class UTF16Charset(name: String) : FixedSizeCharset(name, 2) {
            public abstract val byteOrderMark: Short

            internal abstract fun charToShort(c: Char): Short
            internal abstract fun shortToChar(s: Short): Char

            override fun encode(char: Char, dst: Writeable): Int {
                dst.writeShort(charToShort(char))
                return 2
            }
            override fun decode(src: Readable): Char = shortToChar(src.readShort())
            override fun tryDecode(src: Readable): Int =
                shortToChar(slowLoadShort {
                    src.tryReadByte().also { if (it < 0) return it }.toByte()
                }).toInt()
        }
    }

    public object ASCII : Type.ByteCharset("ASCII", 0x7F)

    @Suppress("ClassName")
    public object ISO8859_1 : Type.ByteCharset("ISO-8859-1", 0xFF)

    @Suppress("ClassName")
    public object ISO8859_15 : Type.ByteCharset("ISO-8859-15", 0xFF) {
        override fun charToInt(c: Char): Int = when (c) {
            '€' -> 0xA4
            'Š' -> 0xA6
            'š' -> 0xA8
            'Ž' -> 0xB4
            'ž' -> 0xB8
            'Œ' -> 0xBC
            'œ' -> 0xBD
            'Ÿ' -> 0xBE
            else -> c.toInt()
        }

        override fun byteToChar(b: Byte): Char = when (b) {
            0xA4.toByte() -> '€'
            0xA6.toByte() -> 'Š'
            0xA8.toByte() -> 'š'
            0xB4.toByte() -> 'Ž'
            0xB8.toByte() -> 'ž'
            0xBC.toByte() -> 'Œ'
            0xBD.toByte() -> 'œ'
            0xBE.toByte() -> 'Ÿ'
            else -> b.toChar()
        }
    }

    public object UTF16BE : Type.UTF16Charset("UTF-16BE") {
        override fun headerSize(): Int = 0
        override val byteOrderMark: Short get() = 0xFEFF.toShort()
        override fun charToShort(c: Char): Short = c.toShort()
        override fun shortToChar(s: Short): Char = s.toChar()
    }

    public object UTF16LE : Type.UTF16Charset("UTF-16LE") {
        override fun headerSize(): Int = 0
        override val byteOrderMark: Short get() = 0xFFFE.toShort()
        override fun charToShort(c: Char): Short = swapEndian(c.toShort())
        override fun shortToChar(s: Short): Char = swapEndian(s).toChar()
    }

    public class UTF16(public val encoder: Type.UTF16Charset = UTF16BE) : Type.FixedSizeCharset(name, 2) {
        override fun headerSize(): Int = 2
        private var hasWrittenMark = false
        override fun encode(char: Char, dst: Writeable): Int {
            if (!hasWrittenMark) {
                dst.writeShort(encoder.byteOrderMark)
                hasWrittenMark = true
            }
            encoder.encode(char, dst)
            return 2
        }
        private var decoder: Type.UTF16Charset? = null
        override fun decode(src: Readable): Char {
            if (decoder == null) {
                when (val mark = src.readShort()) {
                    UTF16BE.byteOrderMark -> decoder = UTF16BE
                    UTF16LE.byteOrderMark -> decoder = UTF16LE
                    else -> {
                        decoder = UTF16BE
                        return decoder!!.shortToChar(mark)
                    }
                }
            }
            return decoder!!.decode(src)
        }

        override fun tryDecode(src: Readable): Int {
            if (decoder == null) {
                val mark = slowLoadShort { src.tryReadByte().also { if (it < 0) return it }.toByte() }
                when (mark) {
                    UTF16BE.byteOrderMark -> decoder = UTF16BE
                    UTF16LE.byteOrderMark -> decoder = UTF16LE
                    else -> {
                        decoder = UTF16BE
                        return decoder!!.shortToChar(mark).toInt()
                    }
                }
            }
            return decoder!!.tryDecode(src)
        }
        public companion object {
            public val name: String = "UTF-16"
        }
    }

    public object UTF8 : Charset("UTF-8") {
        override fun headerSize(): Int = 0

        override fun sizeOf(char: Char): Int {
            val code = char.toInt()
            return when {
                code and 0x7F.inv() == 0 -> 1
                code and 0x7FF.inv() == 0 -> 2
                code and 0xFFFF.inv() == 0 -> 3
                else -> throw IllegalStateException("Unsupported character '$char' (0x${char.toInt().toString(radix = 16)})")
            }
        }

        override fun encode(char: Char, dst: Writeable): Int {
            fun createByte(code: Int, shift: Int): Int = code shr shift and 0x3F or 0x80

            val code = char.toInt()
            if (code and 0x7F.inv() == 0) { // 1-byte sequence
                dst.writeByte(code.toByte())
                return 1
            }

            val count = when {
                code and 0x7FF.inv() == 0 -> { // 2-byte sequence
                    dst.writeByte((code shr 6 and 0x1F or 0xC0).toByte())
                    2
                }
                code and 0xFFFF.inv() == 0 -> { // 3-byte sequence
                    dst.writeByte((code shr 12 and 0x0F or 0xE0).toByte())
                    dst.writeByte((createByte(code, 6)).toByte())
                    3
                }
                // It is impossible to handle 4 bytes character as they
                // do not exist in UTF-16 (which the Char type represents).
                else -> throw IllegalStateException("Unsupported character")
            }
            dst.writeByte((code and 0x3F or 0x80).toByte())
            return count
        }

        private inline fun decode(readByte: () -> Byte): Char {
            val c0 = readByte().toInt() and 0xFF
            when (c0 shr 4) {
                in 0..7 -> { // 0xxxxxxx
                    return c0.toChar()
                }
                in 12..13 -> { // 110x xxxx   10xx xxxx
                    val c1 = readByte().toInt()
                    return (c0 and 0x1F shl 6 or (c1 and 0x3F)).toChar()
                }
                14 -> { // 1110 xxxx  10xx xxxx  10xx xxxx
                    val c1 = readByte().toInt()
                    val c2 = readByte().toInt()
                    return (c0 and 0x0F shl 12 or (c1 and 0x3F shl 6) or (c2 and 0x3F)).toChar()
                }
                else -> throw IllegalStateException("Unsupported character")
            }
        }

        override fun decode(src: Readable): Char =
                decode { src.readByte() }

        override fun tryDecode(src: Readable): Int =
                decode {
                    val b = src.tryReadByte()
                    if (b == -1) return -1
                    b.toByte()
                }.toInt()
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
