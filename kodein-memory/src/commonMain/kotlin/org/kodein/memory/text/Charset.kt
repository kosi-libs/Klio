package org.kodein.memory.text

import org.kodein.memory.io.Readable
import org.kodein.memory.io.Writeable
import org.kodein.memory.io.slowLoadShort


public abstract class Charset(public val name: String) {

    public abstract fun sizeOf(char: Char): Int
    public abstract fun encode(char: Char, dst: Writeable): Int
    public abstract fun decode(src: Readable): Char
    public abstract fun tryDecode(src: Readable): Int

    public object Type {
        public abstract class FixedSizeCharset(name: String, public val bytesPerChar: Int) : Charset(name) {
            final override fun sizeOf(char: Char): Int = bytesPerChar
        }

        public abstract class ByteCharset(name: String, private val max: Int) : FixedSizeCharset(name, bytesPerChar = 1) {
            private fun check(char: Char) {
                if (char.toInt() !in 0..max) error("Character '$char' (0x${char.toInt().toString(radix = 16)}) is not an $name character")
            }
            override fun encode(char: Char, dst: Writeable): Int {
                check(char)
                dst.putByte(char.toByte())
                return 1
            }
            override fun decode(src: Readable): Char = src.readByte().toChar()
            override fun tryDecode(src: Readable): Int = src.receive()

            public fun stringToBytes(src: String): ByteArray =
                ByteArray(src.length) {
                    val char = src[it]
                    check(char)
                    char.toByte()
                }
            public fun bytesToString(src: ByteArray): String = CharArray(src.size) { src[it].toChar() }.concatToString()
        }
    }

    public object ASCII : Type.ByteCharset("ASCII", 0x7F)

    @Suppress("ClassName")
    public object ISO8859_1 : Type.ByteCharset("ISO-8859-1", 0xFF)

    public object UTF16 : Type.FixedSizeCharset("UTF-16", bytesPerChar = 2) {
        override fun encode(char: Char, dst: Writeable): Int {
            dst.putShort(char.toShort())
            return 2
        }
        override fun decode(src: Readable): Char = src.readShort().toChar()
        override fun tryDecode(src: Readable): Int =
            slowLoadShort {
                src.receive().also { if (it < 0) return it }.toByte()
            }.toInt()
    }

    public object UTF8 : Charset("UTF-8") {
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
                dst.putByte(code.toByte())
                return 1
            }

            val count = when {
                code and 0x7FF.inv() == 0 -> { // 2-byte sequence
                    dst.putByte((code shr 6 and 0x1F or 0xC0).toByte())
                    2
                }
                code and 0xFFFF.inv() == 0 -> { // 3-byte sequence
                    dst.putByte((code shr 12 and 0x0F or 0xE0).toByte())
                    dst.putByte((createByte(code, 6)).toByte())
                    3
                }
                // It is impossible to handle 4 bytes character as they
                // do not exist in UTF-16 (which the Char type represents).
                else -> throw IllegalStateException("Unsupported character")
            }
            dst.putByte((code and 0x3F or 0x80).toByte())
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
                decode { src.receive().also { if (it < 0) return it }.toByte() }.toInt()
    }

    public companion object {
        public fun named(name: String): Charset =
            when (name) {
                UTF8.name -> UTF8
                UTF16.name -> UTF16
                ASCII.name -> ASCII
                ISO8859_1.name -> ISO8859_1
                else -> throw NoSuchElementException("Unknown charset $name")
            }
    }
}
