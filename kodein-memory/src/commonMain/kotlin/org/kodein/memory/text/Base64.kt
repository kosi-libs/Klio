package org.kodein.memory.text

import org.kodein.memory.KBuffer
import org.kodein.memory.Readable
import org.kodein.memory.Writeable
import org.kodein.memory.wrap
import kotlin.math.min


object Base64 {
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


    val encoder get() = Base64.Encoder.RFC4648
    val urlEncoder get() = Base64.Encoder.RFC4648_URLSAFE
    val mimeEncoder get() = Base64.Encoder.RFC2045
    fun mimeEncoder(lineLength: Int, lineSeparator: String): Encoder {
        for (b in lineSeparator) {
            if (fromBase64[b.toInt() and 0xff] != -1)
                throw IllegalArgumentException("Illegal base64 line separator character 0x" + b.toInt().toString(16))
        }
        return if (lineLength <= 0) {
            Base64.Encoder.RFC4648
        } else {
            Encoder(false, lineSeparator, lineLength shr 2 shl 2, true)
        }
    }

    val decoder get() = Base64.Decoder.RFC4648
    val urlDecoder get() = Base64.Decoder.RFC4648_URLSAFE
    val mimeDecoder get() = Base64.Decoder.RFC2045

    class Encoder internal constructor(private val isURL: Boolean, private val newline: String?, private val linemax: Int, private val doPadding: Boolean) {

        fun outLength(srclen: Int): Int {
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

        fun withoutPadding(): Encoder = if (!doPadding) this else Encoder(isURL, newline, linemax, false)

        fun encode(src: ByteArray, off: Int = 0, len: Int = src.size - off): String = encode(KBuffer.wrap(src, off, len))

        fun encode(src: ByteArray, dst: Writeable, off: Int = 0, len: Int = src.size - off): Int = encode(KBuffer.wrap(src, off, len), dst)

        fun encode(src: Readable, length: Int = src.remaining): String {
            val dst = ByteArray(outLength(length))
            val realLength = encode(src, KBuffer.wrap(dst))
            return String(CharArray(realLength) { dst[it].toChar() })
        }

        fun encode(src: Readable, dst: Writeable, len: Int = src.remaining): Int {
            val base64 = if (isURL) toBase64URL else toBase64
            var sp = 0
            var slen = len / 3 * 3
            val sl = slen
            if (linemax > 0 && slen > linemax / 4 * 3)
                slen = linemax / 4 * 3
            var dp = 0
            while (sp < sl) {
                val sl0 = min(sp + slen, sl)
                var sp0 = sp
                while (sp0 < sl0) {
                    val bits =
                            (src.read().toInt() and 0xff shl 16) or
                            (src.read().toInt() and 0xff shl 8) or
                            (src.read().toInt() and 0xff)
                    sp0 += 3
                    dst.put(base64[bits ushr 18 and 0x3f].toByte())
                    dst.put(base64[bits ushr 12 and 0x3f].toByte())
                    dst.put(base64[bits ushr 6 and 0x3f].toByte())
                    dst.put(base64[bits and 0x3f].toByte())
                }
                val dlen = (sl0 - sp) / 3 * 4
                dp += dlen
                sp = sl0
                if (dlen == linemax && sp < len && newline != null) {
                    for (b in newline) {
                        dst.put(b.toByte())
                        dp++
                    }
                }
            }
            if (sp < len) {
                val b0 = src.read().toInt() and 0xff
                sp++
                dst.put(base64[b0 shr 2].toByte())
                dp++
                if (sp == len) {
                    dst.put(base64[b0 shl 4 and 0x3f].toByte())
                    dp++
                    if (doPadding) {
                        repeat(2) { dst.put('='.toByte()) }
                        dp += 2
                    }
                } else {
                    val b1 = src.peek().toInt() and 0xff
                    dst.put(base64[b0 shl 4 and 0x3f or (b1 shr 4)].toByte())
                    dst.put(base64[b1 shl 2 and 0x3f].toByte())
                    dp += 2
                    if (doPadding) {
                        dst.put('='.toByte())
                        dp++
                    }
                }
            }
            return dp
        }

        companion object {
            private val MIMELINEMAX = 76
            private val CRLF = "\r\n"

            internal val RFC4648 = Encoder(false, null, -1, true)
            internal val RFC4648_URLSAFE = Encoder(true, null, -1, true)
            internal val RFC2045 = Encoder(false, CRLF, MIMELINEMAX, true)
        }
    }

    class Decoder internal constructor(private val isURL: Boolean, private val isMIME: Boolean) {

        fun outLength(src: Readable, len: Int = src.remaining): Int {
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
                    var b = src.read().toInt() and 0xff
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
                if (src.read() == '='.toByte())
                    paddings++
                if (src.read() == '='.toByte())
                    paddings++
            }
            if (paddings == 0 && slen and 0x3 != 0)
                paddings = 4 - (slen and 0x3)
            return 3 * ((slen + 3) / 4) - paddings
        }

        fun decode(src: String): ByteArray {
            val dst = ByteArray(src.length)
            val len = decode(src, KBuffer.wrap(dst))
            return if (len != dst.size) dst.copyOf(len) else dst
        }

        fun decode(src: String, dst: Writeable): Int {
            val buffer = KBuffer.wrap(ByteArray(src.length) { src[it].toByte() })
            return decode(buffer, dst)
        }

        fun decode(src: Readable, length: Int = src.remaining): ByteArray {
            val dst = ByteArray(src.remaining)
            val realLength = decode(src, KBuffer.wrap(dst), length)
            return if (realLength != dst.size) dst.copyOf(realLength) else dst
        }

        fun decode(src: Readable, dst: Writeable, len: Int = src.remaining): Int {
            var sp = 0
            val base64 = if (isURL) fromBase64URL else fromBase64
            var dp = 0
            var bits = 0
            var shiftto = 18
            while (sp < len) {
                val c = src.read().toInt() and 0xff
                sp++
                val b = base64[c]
                if (b < 0) {
                    if (b == -2) {
                        if (shiftto == 6 && (sp == len || src.read() != '='.toByte()) || shiftto == 18) {
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
                    dst.put((bits shr 16).toByte())
                    dst.put((bits shr 8).toByte())
                    dst.put(bits.toByte())
                    dp += 3
                    shiftto = 18
                    bits = 0
                }
            }
            if (shiftto == 6) {
                dst.put((bits shr 16).toByte())
                dp++
            } else if (shiftto == 0) {
                dst.put((bits shr 16).toByte())
                dst.put((bits shr 8).toByte())
                dp += 2
            } else if (shiftto == 12) {
                throw IllegalArgumentException("Last unit does not have enough valid bits")
            }
            while (sp < len) {
                if (isMIME && base64[src.read().toInt()] < 0) {
                    sp++
                    continue
                }
                sp++
                throw IllegalArgumentException("Input byte array has incorrect ending byte at $sp")
            }
            return dp
        }

        companion object {
            internal val RFC4648 = Decoder(false, false)
            internal val RFC4648_URLSAFE = Decoder(true, false)
            internal val RFC2045 = Decoder(false, true)
        }
    }
}