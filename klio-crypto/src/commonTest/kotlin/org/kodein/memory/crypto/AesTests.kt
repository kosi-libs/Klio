package org.kodein.memory.crypto

import org.kodein.memory.io.Memory
import org.kodein.memory.io.array
import org.kodein.memory.io.asWriteable
import org.kodein.memory.text.array
import org.kodein.memory.text.arrayFromHex
import org.kodein.memory.text.toHex
import kotlin.test.Test
import kotlin.test.assertEquals


class AesTests {

    private val key16 = Memory.array("A 16 Byte Key :)")
    private val key24 = Memory.array("A 24 Byte Key for AES :)")
    private val key32 = Memory.array("A 32 Byte Key for AES ciphers :)")

    private val iv = Memory.arrayFromHex("0123456789abcdef0123456789abcdef")

    private val text15 = Memory.array("This is 15 Byte")
    private val text16 = Memory.array("This is 16 Byte!")
    private val text17 = Memory.array("This is 17 Byte!!")

    private val bigText = Memory.array("""
        The Advanced Encryption Standard (AES), also known by its original name Rijndael (Dutch pronunciation: [ˈrɛindaːl]),
        is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology (NIST) in 2001.
    """.trimIndent())

    @Test fun encryptTexts() {
        assertEquals("46d296293f534377dede64270aea7e4e", AES128.encrypt(CipherMode.CBC(iv), key16, text15).toHex())
        assertEquals("309eed6b813a26e5b17339ad22e967676cbf0b77b5c77fcdf04704bf790ed294", AES128.encrypt(CipherMode.CBC(iv), key16, text16).toHex())
        assertEquals("34a690cf2428c521e3bf3a130aed44b72eb7adf4cd75bcdba5e8563336e5bb51", AES128.encrypt(CipherMode.CBC(iv), key16, text17).toHex())
        assertEquals(
            "ea87c3c4b39d29ac38f2e0e8e4ab39271b19a438e18c7350d63835242dac9a7c2fede7e2458da2d2ff47eccbb4b77edffb73ee970da962716538f25ec2239149" +
                    "1102716cc1096c94c1871ff471fd22f99ce52589761b3533673eabe932c9452a8de4bd4d27e1dc9bf96bdd6b694a92d07b8aa64a13b5c645e9dfc804d2596c01" +
                    "57465ab171ead549f27afd4f9fbf48e5225197db98133770cd2b3b02affd62d511bad3af86b91d8187f95ecefc6dbbdddad1212d26daeb4bfe93af3ab59f88a0" +
                    "888fbf239eda0ea38212a3abf499e4c522453f7c3e923f28b027e03e9c10589a884ccbc33a778322339df689fed34a12f9de273c0090cb1f3c6891a13c89a723" +
                    "dd8dbc614564ab82b093648efea829d1",
            AES128.encrypt(CipherMode.CBC(iv), key16, bigText).toHex()
        )

        assertEquals("17fb118aeb9f8785339ff26d36479acd", AES128.encrypt(CipherMode.CBC(iv), key24, text15).toHex())
        assertEquals("84ffd4094bad986ff59d0c691fb08d5f2eec49a98c092038726985c81c25a848", AES128.encrypt(CipherMode.CBC(iv), key24, text16).toHex())
        assertEquals("879c93a11ee91e4564cce63dd5b080f51d4b75cda77250a1913c81735235256f", AES128.encrypt(CipherMode.CBC(iv), key24, text17).toHex())
        assertEquals(
                    "5faaa909c5dd1754f0508b8d57572e282a8dd667ed834eef26e3e6b8256570b3aee9650d3d8383904b1de91e8744035c5e8866f950fb0d14b264d00287f6b6ed" +
                            "9ae56fc22bcb082c20566e7c6d7bb85d8616fcd50048bfb9347d53a92219e7414e207c21883829d9ea61ef81e0f23c69ad6f072a47057be9f8de43c432473af4" +
                            "31668733bcd97c42842240e5d28173de19a1f954a0372bdb3526525d6e776b1165d2c64baffd5fcc24cf298311181ba5c8c8c95222c11e1b3d6646228308fd62" +
                            "beb0a775d930dd350313cf95466584f889ca9e95ca07700af3584400ceb2bc08616fc04ac4cce6d6b9d53591308c1255f15e052d477601135160357d26dbd015" +
                            "3470a513c7c72f19a60600c2ab2f603a",
            AES128.encrypt(CipherMode.CBC(iv), key24, bigText).toHex()
        )

        assertEquals("902aae6c914573c672b6b5d27790e826", AES128.encrypt(CipherMode.CBC(iv), key32, text15).toHex())
        assertEquals("941dc4ea5141df1310cd1bb9cf044c51a81b889d851478bc9c6fff67a252086f", AES128.encrypt(CipherMode.CBC(iv), key32, text16).toHex())
        assertEquals("ab45a5aece22cc5de1b1a2389de730d156fabdc548d1687b003e3cf1428e6014", AES128.encrypt(CipherMode.CBC(iv), key32, text17).toHex())
        assertEquals(
            "697d8ca31d79c19195824a2c8078d9c9be001ac3ef0d358dec306259f295da01ff2b4ba67ab93362c178b14348eca889c6232f3a474dd3382abc27508cbd0f1d" +
                    "f68f851b7dc634faa317226f7889ee530008e63cf5acc1b380fe942d6b80bc7df0f54080b78ef4e3e95f05f14e4a27fa68a5771f0a3fef607901d4d54829ffac" +
                    "de22d63313e03690b9bbd94e55c14bfa0dc53fa123092b7be3d170076a4cfe7d6ec617dfc871ee42491a86c90b007ceca60fb4f7cedb06dda78e090ad9014925" +
                    "40746160c5c00f878ed7ce6f9e87c318ff86fdd8b8ef0483b79ff7bffcd3cca30ca6eefd966b1d1e5f28d42cca80a7aeeab2474036223e448ac9f2a5e5161ea8" +
                    "2694630ff7d3239a05af41f7808d90c3",
            AES128.encrypt(CipherMode.CBC(iv), key32, bigText).toHex()
        )
    }

    @Test fun encryptValues() {
        run {
            val output = Memory.array(16)
            AES128.encrypt(CipherMode.CBC(iv), key16, output.asWriteable()) {
                writeByte(0x54)
                writeShort(0x6869)
                writeInt(0x73206973)
                writeLong(0x2031352042797465)
            }
            assertEquals("46d296293f534377dede64270aea7e4e", output.toHex())
        }
        run {
            val output = Memory.array(16)
            AES128.encrypt(CipherMode.CBC(iv), key24, output.asWriteable()) {
                writeByte(0x54)
                writeShort(0x6869)
                writeInt(0x73206973)
                writeLong(0x2031352042797465)
            }
            assertEquals("17fb118aeb9f8785339ff26d36479acd", output.toHex())
        }
        run {
            val output = Memory.array(16)
            AES128.encrypt(CipherMode.CBC(iv), key32, output.asWriteable()) {
                writeByte(0x54)
                writeShort(0x6869)
                writeInt(0x73206973)
                writeLong(0x2031352042797465)
            }
            assertEquals("902aae6c914573c672b6b5d27790e826", output.toHex())
        }
    }

    @Test fun decryptTexts() {
        assertEquals(text15.toHex(), AES128.decrypt(CipherMode.CBC(iv), key16, Memory.arrayFromHex("46d296293f534377dede64270aea7e4e")).toHex())
        assertEquals(text16.toHex(), AES128.decrypt(CipherMode.CBC(iv), key16, Memory.arrayFromHex("309eed6b813a26e5b17339ad22e967676cbf0b77b5c77fcdf04704bf790ed294")).toHex())
        assertEquals(text17.toHex(), AES128.decrypt(CipherMode.CBC(iv), key16, Memory.arrayFromHex("34a690cf2428c521e3bf3a130aed44b72eb7adf4cd75bcdba5e8563336e5bb51")).toHex())
        assertEquals(
            bigText.toHex(),
            AES128.decrypt(CipherMode.CBC(iv), key16, Memory.arrayFromHex(
                    "ea87c3c4b39d29ac38f2e0e8e4ab39271b19a438e18c7350d63835242dac9a7c2fede7e2458da2d2ff47eccbb4b77edffb73ee970da962716538f25ec2239149" +
                        "1102716cc1096c94c1871ff471fd22f99ce52589761b3533673eabe932c9452a8de4bd4d27e1dc9bf96bdd6b694a92d07b8aa64a13b5c645e9dfc804d2596c01" +
                        "57465ab171ead549f27afd4f9fbf48e5225197db98133770cd2b3b02affd62d511bad3af86b91d8187f95ecefc6dbbdddad1212d26daeb4bfe93af3ab59f88a0" +
                        "888fbf239eda0ea38212a3abf499e4c522453f7c3e923f28b027e03e9c10589a884ccbc33a778322339df689fed34a12f9de273c0090cb1f3c6891a13c89a723" +
                        "dd8dbc614564ab82b093648efea829d1")
            ).toHex()
        )

        assertEquals(text15.toHex(), AES128.decrypt(CipherMode.CBC(iv), key24, Memory.arrayFromHex("17fb118aeb9f8785339ff26d36479acd")).toHex())
        assertEquals(text16.toHex(), AES128.decrypt(CipherMode.CBC(iv), key24, Memory.arrayFromHex("84ffd4094bad986ff59d0c691fb08d5f2eec49a98c092038726985c81c25a848")).toHex())
        assertEquals(text17.toHex(), AES128.decrypt(CipherMode.CBC(iv), key24, Memory.arrayFromHex("879c93a11ee91e4564cce63dd5b080f51d4b75cda77250a1913c81735235256f")).toHex())
        assertEquals(
            bigText.toHex(),
            AES128.decrypt(CipherMode.CBC(iv), key24, Memory.arrayFromHex(
                    "5faaa909c5dd1754f0508b8d57572e282a8dd667ed834eef26e3e6b8256570b3aee9650d3d8383904b1de91e8744035c5e8866f950fb0d14b264d00287f6b6ed" +
                            "9ae56fc22bcb082c20566e7c6d7bb85d8616fcd50048bfb9347d53a92219e7414e207c21883829d9ea61ef81e0f23c69ad6f072a47057be9f8de43c432473af4" +
                            "31668733bcd97c42842240e5d28173de19a1f954a0372bdb3526525d6e776b1165d2c64baffd5fcc24cf298311181ba5c8c8c95222c11e1b3d6646228308fd62" +
                            "beb0a775d930dd350313cf95466584f889ca9e95ca07700af3584400ceb2bc08616fc04ac4cce6d6b9d53591308c1255f15e052d477601135160357d26dbd015" +
                            "3470a513c7c72f19a60600c2ab2f603a")
            ).toHex()
        )

        assertEquals(text15.toHex(), AES128.decrypt(CipherMode.CBC(iv), key32, Memory.arrayFromHex("902aae6c914573c672b6b5d27790e826")).toHex())
        assertEquals(text16.toHex(), AES128.decrypt(CipherMode.CBC(iv), key32, Memory.arrayFromHex("941dc4ea5141df1310cd1bb9cf044c51a81b889d851478bc9c6fff67a252086f")).toHex())
        assertEquals(text17.toHex(), AES128.decrypt(CipherMode.CBC(iv), key32, Memory.arrayFromHex("ab45a5aece22cc5de1b1a2389de730d156fabdc548d1687b003e3cf1428e6014")).toHex())
        assertEquals(
            bigText.toHex(),
            AES128.decrypt(CipherMode.CBC(iv), key32, Memory.arrayFromHex(
                    "697d8ca31d79c19195824a2c8078d9c9be001ac3ef0d358dec306259f295da01ff2b4ba67ab93362c178b14348eca889c6232f3a474dd3382abc27508cbd0f1d" +
                            "f68f851b7dc634faa317226f7889ee530008e63cf5acc1b380fe942d6b80bc7df0f54080b78ef4e3e95f05f14e4a27fa68a5771f0a3fef607901d4d54829ffac" +
                            "de22d63313e03690b9bbd94e55c14bfa0dc53fa123092b7be3d170076a4cfe7d6ec617dfc871ee42491a86c90b007ceca60fb4f7cedb06dda78e090ad9014925" +
                            "40746160c5c00f878ed7ce6f9e87c318ff86fdd8b8ef0483b79ff7bffcd3cca30ca6eefd966b1d1e5f28d42cca80a7aeeab2474036223e448ac9f2a5e5161ea8" +
                            "2694630ff7d3239a05af41f7808d90c3")
            ).toHex()
        )
    }
}
