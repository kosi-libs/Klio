package org.kodein.memory.util

import kotlinx.cinterop.*
import platform.windows.*
import kotlin.random.Random


@OptIn(ExperimentalUnsignedTypes::class)
private object SecureRandomMingw : Random() {

    private inline fun WINBOOL.verify(message: () -> String) { if (this == 0) error(message()) }

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        require(fromIndex in 0..array.size && toIndex in 0..array.size) { "fromIndex ($fromIndex) or toIndex ($toIndex) are out of range: 0..${array.size}." }
        require(fromIndex <= toIndex) { "fromIndex ($fromIndex) must be not greater than toIndex ($toIndex)." }

        val size = toIndex - fromIndex
        memScoped {
            val context = alloc<HCRYPTPROVVar>()
            CryptAcquireContextA(context.ptr, null, null, PROV_RSA_FULL, CRYPT_VERIFYCONTEXT or CRYPT_SILENT.toUInt())
                    .verify { "Could not acquire crypto context." }

            try {
                array.asUByteArray().usePinned { pinnedBytes ->
                    CryptGenRandom(context.value, size.toUInt(), pinnedBytes.addressOf(fromIndex))
                            .verify { "Could not generate crypto random." }
                }
                return array
            } finally {
                CryptReleaseContext(context.value, 0)
            }
        }
    }

    override fun nextInt(): Int {
        val a = ByteArray(Int.SIZE_BYTES)
        nextBytes(a)
        return (a[0].toInt() shl 24) or
                (a[1].toInt() shl 16) or
                (a[2].toInt() shl 8) or
                (a[3].toInt() shl 0)
    }

    override fun nextBits(bitCount: Int): Int = nextInt().takeUpperBits(bitCount)
}

public actual fun Random.Default.secure(): Random = SecureRandomMingw
