package org.kodein.memory.text

import org.kodein.memory.io.*
import kotlin.math.min


public object Base64 {
    private val toBase64    = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/')
    private val toBase64URL = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_')

    private val fromBase64 = IntArray(256) { -1 }
    private val fromBase64URL = IntArray(256) { -1 }

    init {
        for (i in toBase64.indices) {
            fromBase64[toBase64[i].toInt()] = i
        }
        fromBase64['='.toInt()] = -2
        for (i in toBase64URL.indices) {
            fromBase64URL[toBase64URL[i].toInt()] = i
        }
        fromBase64URL['='.toInt()] = -2
    }


    public val encoder: Encoder get() = Encoder.RFC4648
    public val urlEncoder: Encoder get() = Encoder.RFC4648_URLSAFE
    public val mimeEncoder: Encoder get() = Encoder.RFC2045
    public fun mimeEncoder(lineLength: Int, lineSeparator: String): Encoder {
        for (b in lineSeparator) {
            if (fromBase64[b.toInt() and 0xff] != -1)
                throw IllegalArgumentException("Illegal base64 line separator character 0x" + b.toInt().toString(16))
        }
        return if (lineLength <= 0) {
            Encoder.RFC4648
        } else {
            Encoder(false, lineSeparator, lineLength shr 2 shl 2, true)
        }
    }

    public val decoder: Decoder get() = Decoder.RFC4648
    public val urlDecoder: Decoder get() = Decoder.RFC4648_URLSAFE
    public val mimeDecoder: Decoder get() = Decoder.RFC2045

    public class Encoder internal constructor(private val isURL: Boolean, private val newline: String?, private val linemax: Int, private val doPadding: Boolean) {

        public fun outLength(srclen: Int): Int {
            val len = if (doPadding) {
                4 * ((srclen + 2) / 3)
            } else {
                val n = srclen % 3
                4 * (srclen / 3) + if (n == 0) 0 else n + 1
            }
            if (linemax > 0 && newline != null) {
                return len + (len - 1) / linemax * newline.length
            }
            return len
        }

        public fun withoutPadding(): Encoder = if (!doPadding) this else Encoder(isURL, newline, linemax, false)

        public fun encode(src: ByteArray, off: Int = 0, len: Int = src.size - off): String = encode(Memory.wrap(src, off, len).asReadable())
        public fun encode(src: Memory, offset: Int = 0, length: Int = src.size - offset): String = encode(src.slice(offset, length).asReadable())
        public fun encode(src: CursorReadable): String = encode(src, src.remaining)

        public fun encode(src: Readable, length: Int): String {
            val dst = ByteArray(outLength(length))
            val realLength = encode(Memory.wrap(dst).asWriteable(), src, length)
            return CharArray(realLength) { dst[it].toChar() }.concatToString()
        }

        public fun encode(dst: Writeable, src: ByteArray, offset: Int = 0, length: Int = src.size - offset): Int = encode(dst, Memory.wrap(src, offset, length).asReadable())
        public fun encode(dst: Writeable, src: Memory, offset: Int = 0, length: Int = src.size - offset): Int = encode(dst, src.slice(offset, length).asReadable())
        public fun encode(dst: Writeable, src: CursorReadable): Int = encode(dst, src, src.remaining)

        public fun encode(dst: Writeable, src: Readable, length: Int): Int {
            val base64 = if (isURL) toBase64URL else toBase64
            var sp = 0
            var slen = length / 3 * 3
            val sl = slen
            if (linemax > 0 && slen > linemax / 4 * 3)
                slen = linemax / 4 * 3
            var dp = 0
            while (sp < sl) {
                val sl0 = min(sp + slen, sl)
                var sp0 = sp
                while (sp0 < sl0) {
                    val bits =
                            (src.readByte().toInt() and 0xff shl 16) or
                            (src.readByte().toInt() and 0xff shl 8) or
                            (src.readByte().toInt() and 0xff)
                    sp0 += 3
                    dst.writeByte(base64[bits ushr 18 and 0x3f].toByte())
                    dst.writeByte(base64[bits ushr 12 and 0x3f].toByte())
                    dst.writeByte(base64[bits ushr 6 and 0x3f].toByte())
                    dst.writeByte(base64[bits and 0x3f].toByte())
                }
                val dlen = (sl0 - sp) / 3 * 4
                dp += dlen
                sp = sl0
                if (dlen == linemax && sp < length && newline != null) {
                    for (b in newline) {
                        dst.writeByte(b.toByte())
                        dp++
                    }
                }
            }
            if (sp < length) {
                val b0 = src.readByte().toInt() and 0xff
                sp++
                dst.writeByte(base64[b0 shr 2].toByte())
                dp++
                if (sp == length) {
                    dst.writeByte(base64[b0 shl 4 and 0x3f].toByte())
                    dp++
                    if (doPadding) {
                        repeat(2) { dst.writeByte('='.toByte()) }
                        dp += 2
                    }
                } else {
                    val b1 = src.readByte().toInt() and 0xff
                    dst.writeByte(base64[b0 shl 4 and 0x3f or (b1 shr 4)].toByte())
                    dst.writeByte(base64[b1 shl 2 and 0x3f].toByte())
                    dp += 2
                    if (doPadding) {
                        dst.writeByte('='.toByte())
                        dp++
                    }
                }
            }
            return dp
        }

        public companion object {
            private val MIMELINEMAX = 76
            private val CRLF = "\r\n"

            internal val RFC4648 = Encoder(false, null, -1, true)
            internal val RFC4648_URLSAFE = Encoder(true, null, -1, true)
            internal val RFC2045 = Encoder(false, CRLF, MIMELINEMAX, true)
        }
    }

    public class Decoder internal constructor(private val isURL: Boolean, private val isMIME: Boolean) {

        public fun outLength(src: Readable, len: Int): Int {
            if (len == 0)
                return 0
            val base64 = if (isURL) fromBase64URL else fromBase64
            if (len < 2) {
                if (isMIME && base64[0] == -1)
                    return 0
                throw IllegalArgumentException("Input byte[] should at least have 2 bytes for base64 bytes")
            }
            var sp = 0
            var slen = len
            var paddings = 0
            if (isMIME) {
                var n = 0
                while (sp < slen) {
                    var b = src.readByte().toInt() and 0xff
                    sp++
                    if (b == '='.toInt()) {
                        slen -= slen - sp + 1
                        break
                    }
                    b = base64[b]
                    if (b == -1)
                        n++
                }
                slen -= n
            } else {
                src.skip(len - 2)
                if (src.readByte() == '='.toByte())
                    paddings++
                if (src.readByte() == '='.toByte())
                    paddings++
            }
            if (paddings == 0 && slen and 0x3 != 0)
                paddings = 4 - (slen and 0x3)
            return 3 * ((slen + 3) / 4) - paddings
        }

        public fun decode(src: String): ByteArray {
            val dst = ByteArray(src.length)
            val len = decode(Memory.wrap(dst).asWriteable(), src)
            return if (len != dst.size) dst.copyOf(len) else dst
        }

        public fun decode(src: Memory, offset: Int = 0, length: Int = src.size - offset): ByteArray = decode(src.slice(offset, length).asReadable())

        public fun decode(src: CursorReadable): ByteArray = decode(src, src.remaining)

        public fun decode(src: Readable, length: Int): ByteArray {
            val dst = ByteArray(length)
            val realLength = decode(Memory.wrap(dst).asWriteable(), src, length)
            return if (realLength != dst.size) dst.copyOf(realLength) else dst
        }

        public fun decode(dst: Writeable, src: String): Int {
            val input = Memory.wrap(ByteArray(src.length) { src[it].toByte() }).asReadable()
            return decode(dst, input, src.length)
        }

        public fun decode(dst: Writeable, src: Memory, offset: Int = 0, length: Int = src.size - offset): Int = decode(dst, src.slice(offset, length).asReadable())

        public fun decode(dst: Writeable, src: CursorReadable): Int = decode(dst, src, src.remaining)

        public fun decode(dst: Writeable, src: Readable, length: Int): Int {
            var sp = 0
            val base64 = if (isURL) fromBase64URL else fromBase64
            var dp = 0
            var bits = 0
            var shiftto = 18
            while (sp < length) {
                val c = src.readByte().toInt() and 0xff
                sp++
                val b = base64[c]
                if (b < 0) {
                    if (b == -2) {
                        if (shiftto == 6 && (sp == length || src.readByte() != '='.toByte()) || shiftto == 18) {
                            throw IllegalArgumentException("Input byte array has wrong 4-byte ending unit")
                        }
                        sp++
                        break
                    }
                    if (isMIME)
                        continue
                    else
                        throw IllegalArgumentException("Illegal base64 character " + c.toString(16))
                }
                bits = bits or (b shl shiftto)
                shiftto -= 6
                if (shiftto < 0) {
                    dst.writeByte((bits shr 16).toByte())
                    dst.writeByte((bits shr 8).toByte())
                    dst.writeByte(bits.toByte())
                    dp += 3
                    shiftto = 18
                    bits = 0
                }
            }
            if (shiftto == 6) {
                dst.writeByte((bits shr 16).toByte())
                dp++
            } else if (shiftto == 0) {
                dst.writeByte((bits shr 16).toByte())
                dst.writeByte((bits shr 8).toByte())
                dp += 2
            } else if (shiftto == 12) {
                throw IllegalArgumentException("Last unit does not have enough valid bits")
            }
            while (sp < length) {
                if (isMIME && base64[src.readByte().toInt()] < 0) {
                    sp++
                    continue
                }
                sp++
                throw IllegalArgumentException("Input byte array has incorrect ending byte at $sp")
            }
            return dp
        }

        public companion object {
            internal val RFC4648 = Decoder(false, false)
            internal val RFC4648_URLSAFE = Decoder(true, false)
            internal val RFC2045 = Decoder(false, true)
        }
    }
}