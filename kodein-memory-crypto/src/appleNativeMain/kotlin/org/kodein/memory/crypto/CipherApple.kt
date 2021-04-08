package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import org.kodein.memory.useOrNull
import platform.CoreCrypto.*
import platform.posix.size_tVar


private fun CCCryptorStatus.checkCoreCryptoSuccess() {
    when (this) {
        kCCSuccess -> return
        kCCParamError -> throw IOException("Illegal parameter value.")
        kCCBufferTooSmall -> throw IOException("Insufficent buffer provided for specified operation.")
        kCCMemoryFailure -> throw IOException("Memory allocation failure.")
        kCCAlignmentError -> throw IOException("Input size was not aligned properly.")
        kCCDecodeError -> throw IOException("Input data did not decode or decrypt properly.")
        kCCUnimplemented -> throw IOException("Function not implemented for the current algorithm.")
        else -> throw IOException("Unknown error code: $this")
    }
}

private class AppleCipherWriteable(private val cryptorRef: CCCryptorRefVar, key: Allocation, output: Writeable) : NativeCipherWriteable(key, output) {
    val outLen = nativeHeap.alloc<size_tVar>()

    override fun doUpdate(inputPtr: CPointer<*>, inputLength: Int, outputPtr: CPointer<*>, outputSize: Int): Int {
        outLen.value = 0.convert()
        CCCryptorUpdate(cryptorRef.value, inputPtr, inputLength.convert(), outputPtr, outputSize.convert(), outLen.ptr).checkCoreCryptoSuccess()
        return outLen.value.convert()
    }

    override fun doFinal(outputPtr: CPointer<*>, outputSize: Int): Int {
        outLen.value = 0.convert()
        CCCryptorFinal(cryptorRef.value, outputPtr, outputSize.convert(), outLen.ptr).checkCoreCryptoSuccess()
        return outLen.value.convert()
    }

    override fun doClose() {
        nativeHeap.free(outLen)
        nativeHeap.free(cryptorRef)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public actual object AES128 {
    private fun getCipher(cipherMode: CipherMode, opMode: UInt, key: ReadMemory, output: Writeable): NativeCipherWriteable {
        require(key.size in arrayOf(16, 24, 32)) { "Key must be 16, 24 or 32 bytes (not ${key.size})." }

        val keyCopy = Allocation.nativeCopy(key)

        val additionalOption = when (cipherMode) {
            is CipherMode.CBC -> 0u
            is CipherMode.ECB -> kCCOptionECBMode
        }

        val iv = when (cipherMode) {
            is CipherMode.CBC -> {
                cipherMode.iv?.let { iv ->
                    require(iv.size == 16) { "IV must be 16 bytes." }
                    Allocation.nativeCopy(iv)
                }
            }
            CipherMode.ECB -> null
        }

        iv.useOrNull {
            val cryptorRef = nativeHeap.alloc<CCCryptorRefVar>()

            CCCryptorCreate(
                opMode,
                kCCAlgorithmAES128,
                kCCOptionPKCS7Padding or additionalOption,
                keyCopy.memory.pointer,
                keyCopy.size.convert(),
                iv?.memory?.pointer,
                cryptorRef.ptr
            ).checkCoreCryptoSuccess()

            return AppleCipherWriteable(cryptorRef, keyCopy, output)
        }
    }

    public actual fun encrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, kCCEncrypt, key, output)

    public actual fun decrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, kCCDecrypt, key, output)
}
