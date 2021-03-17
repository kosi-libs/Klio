package org.kodein.memory.text

import org.kodein.memory.io.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalUnsignedTypes
@Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
class Base64Tests {

    val m21 = ubyteArrayOf(21u, 42u, 63u, 84u, 105u, 126u, 147u, 168u, 189u, 210u, 231u, 252u).asByteArray()
    val s15 = byteArrayOf(1, 2, 3, 4, 5)
    val s14 = byteArrayOf(1, 2, 3, 4)

    @Test
    fun encode() {
        assertEquals("FSo/VGl+k6i90uf8", Base64.encoder.encode(m21))
        assertEquals("FSo_VGl-k6i90uf8", Base64.urlEncoder.encode(m21))

        assertEquals("AQIDBAU=", Base64.encoder.encode(s15))
        assertEquals("AQIDBAU", Base64.encoder.withoutPadding().encode(s15))
        assertEquals("AQIDBA==", Base64.encoder.encode(s14))
        assertEquals("AQIDBA", Base64.encoder.withoutPadding().encode(s14))
    }

    @Test
    fun decode() {
        assertTrue(m21.contentEquals(Base64.decoder.decode("FSo/VGl+k6i90uf8")))
        assertTrue(m21.contentEquals(Base64.urlDecoder.decode("FSo_VGl-k6i90uf8")))
        assertTrue(s15.contentEquals(Base64.decoder.decode("AQIDBAU=")))
        assertTrue(s15.contentEquals(Base64.decoder.decode("AQIDBAU")))
        assertTrue(s14.contentEquals(Base64.decoder.decode("AQIDBA==")))
        assertTrue(s14.contentEquals(Base64.decoder.decode("AQIDBA")))
    }

    @Test
    fun bigText() {
        val src = Memory.array(palindrome)
        val size = src.size

        val base64 = Base64.mimeEncoder.encode(src.asReadable())

        val dst = Memory.array(size)
        val slice = dst.slice { Base64.mimeDecoder.decode(this, base64) }
        val decodedString = slice.asReadable().readString()

        assertEquals(palindrome, decodedString)
    }

}
