package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import platform.windows.*


@OptIn(ExperimentalUnsignedTypes::class)
public actual object Pbkdf2 {

    public actual fun withHmac(digestAlgorithm: DigestAlgorithm, password: ReadMemory, salt: ReadMemory, rounds: Int, dst: Memory) {
        memScoped {
            val nPassword = toReadOnlyCPointerMemory(password)
            val nSalt = toReadOnlyCPointerMemory(salt)
            val nDst = toReadWriteCPointerMemory(dst)

            val hAlg = alloc<BCRYPT_ALG_HANDLEVar>()
            BCryptOpenAlgorithmProvider(hAlg.ptr, digestAlgorithm.toAlgName(), null, BCRYPT_ALG_HANDLE_HMAC_FLAG.toUInt())

            BCryptDeriveKeyPBKDF2(
                    hAlg.value,
                    nPassword.pointer.reinterpret(), nPassword.size.toUInt(),
                    nSalt.pointer.reinterpret(), nSalt.size.toUInt(),
                    rounds.toULong(),
                    nDst.pointer.reinterpret(), nDst.size.toUInt(),
                    0u
            ).requireNTSuccess("BCryptDeriveKeyPBKDF2")

            BCryptCloseAlgorithmProvider(hAlg.value, 0u)
        }
    }
}
