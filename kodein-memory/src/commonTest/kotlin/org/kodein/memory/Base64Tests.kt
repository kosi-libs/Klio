package org.kodein.memory

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
        assertEquals("AQIDBA==", Base64.encoder.encode(s14))
    }

    @Test
    fun decode() {
        assertTrue(m21.contentEquals(Base64.decoder.decode("FSo/VGl+k6i90uf8")))
        assertTrue(m21.contentEquals(Base64.urlDecoder.decode("FSo_VGl-k6i90uf8")))
        assertTrue(s15.contentEquals(Base64.decoder.decode("AQIDBAU=")))
        assertTrue(s14.contentEquals(Base64.decoder.decode("AQIDBA==")))
    }

//    @Test
//    fun loremIpsum() {
//        val lipsumString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus risus elit, efficitur a nisi sit amet, lacinia hendrerit elit. Duis lacinia, eros et eleifend posuere, sapien diam bibendum ex, at blandit quam lacus quis risus. Aliquam id nulla sed risus viverra ullamcorper et a purus. Nullam ullamcorper, felis in laoreet euismod, dolor nisl placerat mauris, eu tincidunt libero enim in lorem. Morbi cursus dui tortor, ut faucibus lorem ultricies iaculis. Vivamus dictum tortor felis, a ultricies magna viverra non. Phasellus non elit et risus imperdiet accumsan sed ut sapien. Aliquam lacinia, elit id dignissim tempor, felis elit sodales ante, sit amet euismod risus dolor vel risus. Proin nisi nunc, porta nec luctus vitae, eleifend sed diam. Donec vitae sapien libero."
//        val lipsumBuffer = KBuffer.wrap(lipsumString.map { it.toByte() }.toByteArray())
//
//        val base64 = Base64.mimeEncoder.encode(lipsumBuffer)
//        val decodedArray = Base64.mimeDecoder.decode(base64)
//        val decodedString = String(decodedArray.map { it.toChar() }.toCharArray())
//
//        assertEquals(lipsumString, decodedString)
//    }
}
