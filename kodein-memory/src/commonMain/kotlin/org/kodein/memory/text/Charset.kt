package org.kodein.memory.text

import org.kodein.memory.Readable
import org.kodein.memory.Writeable

abstract class Charset(val name: String) {

    abstract fun sizeOf(char: Char): Int
    abstract fun encode(char: Char, dst: Writeable): Int
    abstract fun decode(src: Readable): Char

    object ASCII : Charset("ASCII") {
        override fun sizeOf(char: Char) = 1
        override fun encode(char: Char, dst: Writeable): Int {
            dst.put(char.toByte())
            return 1
        }
        override fun decode(src: Readable) = src.read().toChar()
    }

    object UTF16 : Charset("UTF-16") {
        override fun sizeOf(char: Char) = 2
        override fun encode(char: Char, dst: Writeable): Int {
            dst.putShort(char.toShort())
            return 2
        }
        override fun decode(src: Readable) = src.readShort().toChar()
    }

    object UTF8 : Charset("UTF-8") {

        override fun sizeOf(char: Char): Int {
            val code = char.toInt()
            return when {
                code and 0x7F.inv() == 0 -> 1
                code and 0x7FF.inv() == 0 -> 2
                code and 0xFFFF.inv() == 0 -> 3
                else -> throw IllegalStateException("Unsupported character")
            }
        }

        override fun encode(char: Char, dst: Writeable): Int {
            fun createByte(code: Int, shift: Int): Int = code shr shift and 0x3F or 0x80

            val code = char.toInt()
            if (code and 0x7F.inv() == 0) { // 1-byte sequence
                dst.put(code.toByte())
                return 1
            }

            val count = when {
                code and 0x7FF.inv() == 0 -> { // 2-byte sequence
                    dst.put((code shr 6 and 0x1F or 0xC0).toByte())
                    2
                }
                code and 0xFFFF.inv() == 0 -> { // 3-byte sequence
                    dst.put((code shr 12 and 0x0F or 0xE0).toByte())
                    dst.put((createByte(code, 6)).toByte())
                    3
                }
//                code and -0x200000 == 0 -> { // 4-byte sequence
//                    dst.put((code shr 18 and 0x07 or 0xF0).toByte())
//                    dst.put((createByte(code, 12)).toByte())
//                    dst.put((createByte(code, 6)).toByte())
//                }
                else -> throw IllegalStateException("Unsupported character")
            }
            dst.put((code and 0x3F or 0x80).toByte())
            return count
        }

        override fun decode(src: Readable): Char {
            val c0 = src.read().toInt() and 0xFF
            when (c0 shr 4) {
                in 0..7 -> { // 0xxxxxxx
                    return c0.toChar()
                }
                in 12..13 -> { // 110x xxxx   10xx xxxx
                    val c1 = src.read().toInt()
                    return (c0 and 0x1F shl 6 or (c1 and 0x3F)).toChar()
                }
                14 -> { // 1110 xxxx  10xx xxxx  10xx xxxx
                    val c1 = src.read().toInt()
                    val c2 = src.read().toInt()
                    return (c0 and 0x0F shl 12 or (c1 and 0x3F shl 6) or (c2 and 0x3F)).toChar()
                }
                else -> throw IllegalStateException("Unsupported character")
            }
        }
    }

}
