package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import org.kodein.memory.*
import platform.CoreCrypto.*
import platform.KCoreCrypto.KCCKeyDerivationPBKDF


public actual object Pbkdf2 {

    private fun <T : CPointed> MemScope.toNativePointer(memory: ReadMemory): CPointer<T> = when (val m = memory.internalMemory()) {
        is CPointerMemory -> m.pointer
        is ByteArrayMemory -> m.array.refTo(m.offset).getPointer(this)
        else -> {
            val copy = Allocation.nativeCopy(memory)
            defer {
                copy.fill(0)
                copy.close()
            }
            copy.memory.pointer
        }
    }.reinterpret()

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun withHmac(digestAlgorithm: DigestAlgorithm, password: ReadMemory, salt: ReadMemory, rounds: Int, dst: CValuesRef<*>, dstLength: Int) {
        val prfHmacAlgorithm = when (digestAlgorithm) {
            DigestAlgorithm.SHA1 -> kCCPRFHmacAlgSHA1
            DigestAlgorithm.SHA224 -> kCCPRFHmacAlgSHA224
            DigestAlgorithm.SHA256 -> kCCPRFHmacAlgSHA256
            DigestAlgorithm.SHA384 -> kCCPRFHmacAlgSHA384
            DigestAlgorithm.SHA512 -> kCCPRFHmacAlgSHA512
        }

        memScoped {
            KCCKeyDerivationPBKDF(
                kCCPBKDF2,
                toNativePointer(password),
                password.size.toULong(),
                toNativePointer(salt),
                salt.size.toULong(),
                prfHmacAlgorithm,
                rounds.toUInt(),
                dst.getPointer(this).reinterpret(),
                dstLength.toULong()
            )
        }
    }

    public actual fun withHmac(digestAlgorithm: DigestAlgorithm, password: ReadMemory, salt: ReadMemory, rounds: Int, dst: Memory) {
        when (val m = dst.internalMemory()) {
            is CPointerMemory -> withHmac(digestAlgorithm, password, salt, rounds, m.pointer, m.size)
            is ByteArrayMemory -> withHmac(digestAlgorithm, password, salt, rounds, m.array.refTo(m.offset), m.size)
            else -> Allocation.native(m.size).use { buffer ->
                withHmac(digestAlgorithm, password, salt, rounds, buffer.memory.pointer, m.size)
                dst.putBytes(0, buffer)
            }
        }
    }
}
