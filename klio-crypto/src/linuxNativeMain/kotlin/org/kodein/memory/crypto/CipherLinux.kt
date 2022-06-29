package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.crypto.libssl.*
import org.kodein.memory.io.*
import org.kodein.memory.useOrNull
import platform.posix.memcpy


private inline fun output(maxSize: Int, outputPtr: CPointer<*>, outputSize: Int, block: (outputPtr: CPointer<*>) -> Int): Int {
    if (outputSize >= maxSize) return block(outputPtr)

    memScoped {
        val buffer = allocArray<ByteVar>(maxSize)
        val size = block(buffer)
        if (size > outputSize) error("Output memory too small (missing at least ${size - outputSize} bytes).")
        memcpy(outputPtr, buffer, size.convert())
        return size
    }
}

private class LinuxCipherEncryptWriteable(private val ctx: CValuesRef<EVP_CIPHER_CTX>, key: Allocation, output: Writeable) : NativeCipherWriteable(key, output) {
    val outLen = nativeHeap.alloc<IntVar>()

    override fun doUpdate(inputPtr: CPointer<*>, inputLength: Int, outputPtr: CPointer<*>, outputSize: Int): Int =
        output(inputLength + 16 - 1, outputPtr, outputSize) {
            outLen.value = 0
            EVP_EncryptUpdate(ctx, it.reinterpret(), outLen.ptr, inputPtr.reinterpret(), inputLength).requireOpenSSLSuccess("EVP_EncryptUpdate")
            outLen.value
        }

    override fun doFinal(outputPtr: CPointer<*>, outputSize: Int): Int =
        output(16, outputPtr, outputSize) {
            outLen.value = 0
            EVP_EncryptFinal_ex(ctx, it.reinterpret(), outLen.ptr).requireOpenSSLSuccess("EVP_EncryptFinal_ex")
            return outLen.value
        }

    override fun doClose() {
        nativeHeap.free(outLen)
        EVP_CIPHER_CTX_free(ctx)
    }
}

private class LinuxCipherDecryptWriteable(private val ctx: CValuesRef<EVP_CIPHER_CTX>, key: Allocation, output: Writeable) : NativeCipherWriteable(key, output) {
    val outLen = nativeHeap.alloc<IntVar>()

    override fun doUpdate(inputPtr: CPointer<*>, inputLength: Int, outputPtr: CPointer<*>, outputSize: Int): Int =
        output(inputLength + 16, outputPtr, outputSize) {
            outLen.value = 0
            EVP_DecryptUpdate(ctx, it.reinterpret(), outLen.ptr, inputPtr.reinterpret(), inputLength).requireOpenSSLSuccess("EVP_DecryptUpdate")
            outLen.value
    }

    override fun doFinal(outputPtr: CPointer<*>, outputSize: Int): Int =
        output(16, outputPtr, outputSize) {
            outLen.value = 0
            EVP_DecryptFinal_ex(ctx, it.reinterpret(), outLen.ptr).requireOpenSSLSuccess("EVP_DecryptFinal_ex")
            return outLen.value
        }

    override fun doClose() {
        nativeHeap.free(outLen)
        EVP_CIPHER_CTX_free(ctx)
    }
}

private typealias cipherInit = (CValuesRef<EVP_CIPHER_CTX>?, CValuesRef<EVP_CIPHER>?, CValuesRef<ENGINE>?, CValuesRef<UByteVar>?, CValuesRef<UByteVar>?) -> Int

@OptIn(ExperimentalUnsignedTypes::class)
public actual object AES128 {
    private fun getCipher(
        cipherMode: CipherMode,
        key: ReadMemory,
        output: Writeable,
        initFunction: cipherInit,
        initFunctionName: String,
        factory: (CValuesRef<EVP_CIPHER_CTX>, Allocation, Writeable) -> NativeCipherWriteable
    ): NativeCipherWriteable {
        require(key.size in arrayOf(16, 24, 32)) { "Key must be 16, 24 or 32 bytes (not ${key.size})." }

        val keyCopy = Allocation.nativeCopy(key)

        val (cipher, iv) = when (cipherMode) {
            is CipherMode.CBC -> {
                val cipher = when (key.size) {
                    16 -> EVP_aes_128_cbc()
                    24 -> EVP_aes_192_cbc()
                    32 -> EVP_aes_256_cbc()
                    else -> error("Bad key length")
                }
                val iv = cipherMode.iv?.let { iv ->
                    require(iv.size == 16) { "IV must be 16 bytes." }
                    Allocation.nativeCopy(iv)
                }
                cipher to iv
            }
            CipherMode.ECB -> {
                val cipher = when (key.size) {
                    16 -> EVP_aes_128_ecb()
                    24 -> EVP_aes_192_ecb()
                    32 -> EVP_aes_256_ecb()
                    else -> error("Bad key length")
                }
                cipher to null
            }
        }

        iv.useOrNull {
            val ctx = EVP_CIPHER_CTX_new()!!
            initFunction(ctx, cipher, null, keyCopy.memory.pointer.reinterpret(), iv?.memory?.pointer?.reinterpret()).requireOpenSSLSuccess(initFunctionName)

            return factory(ctx, keyCopy, output)
        }
    }

    public actual fun encrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
            getCipher(mode, key, output, ::EVP_EncryptInit_ex, "EVP_EncryptInit_ex", ::LinuxCipherEncryptWriteable)

    public actual fun decrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
            getCipher(mode, key, output, ::EVP_DecryptInit_ex, "EVP_DecryptInit_ex", ::LinuxCipherDecryptWriteable)
}
