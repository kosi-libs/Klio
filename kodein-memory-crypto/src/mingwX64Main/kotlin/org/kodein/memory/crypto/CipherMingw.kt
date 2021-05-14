package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import platform.posix.memcpy
import platform.posix.memset
import platform.windows.*
import kotlin.math.min


private typealias CryptOp = (BCRYPT_KEY_HANDLE?, PUCHAR?, ULONG, CValuesRef<*>?, PUCHAR?, ULONG, PUCHAR?, ULONG, CValuesRef<ULONGVar>?, ULONG) -> NTSTATUS

@OptIn(ExperimentalUnsignedTypes::class)
private class MingwCipherWriteable(
    key: PlatformNativeAllocation,
    private val iv: PlatformNativeAllocation?,
    mode: String,
    output: Writeable,
    private val cryptOp: CryptOp,
    private val opReserve: Int
) : NativeCipherWriteable(key, output) {

    companion object {
        const val bufferSize = 4096
    }

    val heap = Arena()

    val hAlg = heap.alloc<BCRYPT_ALG_HANDLEVar>()
    val hKey = heap.alloc<BCRYPT_KEY_HANDLEVar>()
    val writtenLen = heap.alloc<ULONGVar>()

    val buffer = heap.allocArray<ByteVar>(bufferSize)
    var bufferPosition = 0

    init {
        BCryptOpenAlgorithmProvider(hAlg.ptr, "AES", null, 0u).requireNTSuccess("BCryptOpenAlgorithmProvider")

        val propertySize = heap.alloc<DWORDVar>()
        val keyObjectSize = heap.alloc<DWORDVar>()

        BCryptGetProperty(hAlg.value, "ObjectLength", keyObjectSize.ptr.reinterpret(), DWORD.SIZE_BYTES.toUInt(), propertySize.ptr, 0u).requireNTSuccess("BCryptGetProperty(ObjectLength)")
        val keyObject = nativeHeap.allocArray<ByteVar>(keyObjectSize.value.toInt())

        BCryptGenerateSymmetricKey(hAlg.value, hKey.ptr, keyObject.pointed.reinterpret(), keyObjectSize.value, key.memory.pointer.reinterpret(), key.size.toUInt(), 0u).requireNTSuccess("BCryptGenerateSymmetricKey")

        BCryptSetProperty(hAlg.value, "ChainingMode", mode.wcstr.getPointer(heap).reinterpret(), mode.length.toUInt(), 0u).requireNTSuccess("BCryptSetProperty(ChainingMode)")
    }

    override fun doUpdate(inputPtr: CPointer<*>, inputLength: Int, outputPtr: CPointer<*>, outputSize: Int): Int {
        var totalWrittenLength = 0

        var processed = 0
        while (processed < inputLength) {
            val copySize = min(inputLength - processed, bufferSize - bufferPosition)
            memcpy(buffer + bufferPosition, inputPtr.reinterpret<ByteVar>() + processed, copySize.toULong())
            processed += copySize
            bufferPosition += copySize

            val readySize = ((bufferPosition - opReserve) / 16) * 16
            if (readySize > 0) {
                cryptOp(
                    hKey.value,
                    buffer.reinterpret(), readySize.toUInt(),
                    null,
                    iv?.memory?.pointer?.reinterpret(), (iv?.size ?: 0).toUInt(),
                    (outputPtr.reinterpret<ByteVar>() + totalWrittenLength)!!.reinterpret(), outputSize.toUInt(), writtenLen.ptr,
                    0u
                ).requireNTSuccess("BCryptEncrypt")

                if (readySize < bufferPosition) {
                    memcpy(buffer, buffer + readySize, (bufferPosition - readySize).toULong())
                }
                bufferPosition -= readySize

                totalWrittenLength += writtenLen.value.toInt()
            }
        }

        return totalWrittenLength
    }

    override fun doFinal(outputPtr: CPointer<*>, outputSize: Int): Int {
        cryptOp(
            hKey.value,
            buffer.reinterpret(), bufferPosition.toUInt(),
            null,
            iv?.memory?.pointer?.reinterpret(), (iv?.size ?: 0).toUInt(),
            outputPtr.reinterpret(), outputSize.toUInt(), writtenLen.ptr,
            BCRYPT_BLOCK_PADDING.toUInt()
        ).requireNTSuccess("BCryptEncrypt")
        return writtenLen.value.toInt()
    }

    override fun doClose() {
        BCryptDestroyKey(hKey.value)
        BCryptCloseAlgorithmProvider(hAlg.value, 0u)
        memset(buffer, 0, bufferSize.toULong())
        heap.clear()
        iv?.close()
    }
}

public actual object AES128 {
    private fun getCipher(cipherMode: CipherMode, key: ReadMemory, output: Writeable, cryptOp: CryptOp, opReserve: Int): NativeCipherWriteable {
        val (modeName, iv) = when (cipherMode) {
            is CipherMode.CBC -> "ChainingModeCBC" to cipherMode.iv?.let { Allocation.nativeCopy(it) }
            CipherMode.ECB -> "ChainingModeECB" to null
        }

        val keyCopy = Allocation.nativeCopy(key)

        return MingwCipherWriteable(keyCopy, iv, modeName, output, cryptOp, opReserve)
    }

    public actual fun encrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, key, output, ::BCryptEncrypt, 0)

    public actual fun decrypt(mode: CipherMode, key: ReadMemory, output: Writeable): CipherWriteable =
        getCipher(mode, key, output, ::BCryptDecrypt, 1)
}
