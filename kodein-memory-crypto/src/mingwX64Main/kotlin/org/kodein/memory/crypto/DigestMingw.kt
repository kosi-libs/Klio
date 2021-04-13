package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import platform.windows.*


@OptIn(ExperimentalUnsignedTypes::class)
internal class MingwDigestWriteable(algName: String, val key: PlatformNativeAllocation?) : NativeDigestWriteable() {

    private val hAlg = nativeHeap.alloc<BCRYPT_ALG_HANDLEVar>()
    val hHash = nativeHeap.alloc<BCRYPT_HASH_HANDLEVar>()

    private class HashData(
            val hashObjectSize: Int,
            val hashObject: CPointer<ByteVar>,
            val digestSize: Int
    )

    private val data: HashData = memScoped {
        val propertySize = alloc<DWORDVar>()
        val hashObjectSize = alloc<DWORDVar>()
        val digestSize = alloc<DWORDVar>()

        BCryptOpenAlgorithmProvider(hAlg.ptr, algName, null, if (key != null) BCRYPT_ALG_HANDLE_HMAC_FLAG.toUInt() else 0u)
        BCryptGetProperty(hAlg.value, "ObjectLength", hashObjectSize.ptr.reinterpret(), DWORD.SIZE_BYTES.toUInt(), propertySize.ptr, 0u)
        val hashObject = nativeHeap.allocArray<ByteVar>(hashObjectSize.value.toInt())
        BCryptGetProperty(hAlg.value, "HashDigestLength", digestSize.ptr.reinterpret(), DWORD.SIZE_BYTES.toUInt(), propertySize.ptr, 0u)

        HashData(hashObjectSize.value.convert(), hashObject, digestSize.value.convert())
    }

    override val digestSize: Int get() = data.digestSize

    init {
        doInit()
    }

    private fun doInit() {
        BCryptCreateHash(
                hAlg.value, hHash.ptr,
                data.hashObject.reinterpret(), data.hashObjectSize.toUInt(),
                key?.memory?.pointer?.reinterpret(), key?.size?.convert() ?: 0u,
                0u
        )
    }

    override fun doReset() {
        BCryptDestroyHash(hHash.value)
        doInit()
    }

    override fun doUpdate(dataPtr: CPointer<*>, dataLength: Int) {
        BCryptHashData(hHash.value, dataPtr.reinterpret(), dataLength.toUInt(), 0u)
    }

    override fun doFinal(outputPtr: CPointer<*>) {
        BCryptFinishHash(hHash.value, outputPtr.reinterpret(), digestSize.toUInt(), 0u)
    }

    override fun doClose() {
        BCryptDestroyHash(hHash.value)
        nativeHeap.free(hHash)
        nativeHeap.free(data.hashObject)
        BCryptCloseAlgorithmProvider(hAlg.value, 0u)
        nativeHeap.free(hAlg)
        key?.fill(0)
        key?.close()
    }
}

internal fun DigestAlgorithm.toAlgName(): String = when (this) {
    DigestAlgorithm.SHA1   -> "SHA1"
    DigestAlgorithm.SHA256 -> "SHA256"
    DigestAlgorithm.SHA384 -> "SHA384"
    DigestAlgorithm.SHA512 -> "SHA512"
}

public actual fun DigestWriteable.Companion.newInstance(algorithm: DigestAlgorithm): DigestWriteable =
    MingwDigestWriteable(algorithm.toAlgName(), null)

public actual fun DigestWriteable.Companion.newHmacInstance(algorithm: DigestAlgorithm, key: ReadMemory): DigestWriteable =
    MingwDigestWriteable(algorithm.toAlgName(), Allocation.nativeCopy(key))
