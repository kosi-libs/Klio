package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import org.kodein.memory.crypto.libssl.*
import platform.posix.size_t


private inline fun <C : CVariable> asUpdate(noinline update: (CValuesRef<C>?, CValuesRef<*>?, size_t) -> Unit): NativeDigestUpdateFunction<C> =
    ({ ctx, ptr, size ->
        update(ctx, ptr, size.convert())
    })

private inline fun <C : CVariable> asFinal(noinline final: (CValuesRef<UByteVar>?, CValuesRef<C>?) -> Unit): NativeDigestFinalFunction<C> =
    ({ ctx, output ->
        final(output.reinterpret(), ctx)
    })

public actual fun DigestWriteable.Companion.newInstance(algorithm: DigestAlgorithm): DigestWriteable =
    when (algorithm) {
        DigestAlgorithm.SHA1   -> NativeFunctionsDigestWriteable(20,          nativeHeap.alloc(), ::SHA1_Init,   asUpdate(::SHA1_Update),   asFinal(::SHA1_Final)  ) { nativeHeap.free(it.pointed) }
        DigestAlgorithm.SHA224 -> NativeFunctionsDigestWriteable(SHA224_DIGEST_LENGTH, nativeHeap.alloc(), ::SHA224_Init, asUpdate(::SHA224_Update), asFinal(::SHA224_Final)) { nativeHeap.free(it.pointed) }
        DigestAlgorithm.SHA256 -> NativeFunctionsDigestWriteable(SHA256_DIGEST_LENGTH, nativeHeap.alloc(), ::SHA256_Init, asUpdate(::SHA256_Update), asFinal(::SHA256_Final)) { nativeHeap.free(it.pointed) }
        DigestAlgorithm.SHA384 -> NativeFunctionsDigestWriteable(SHA384_DIGEST_LENGTH, nativeHeap.alloc(), ::SHA384_Init, asUpdate(::SHA384_Update), asFinal(::SHA384_Final)) { nativeHeap.free(it.pointed) }
        DigestAlgorithm.SHA512 -> NativeFunctionsDigestWriteable(SHA512_DIGEST_LENGTH, nativeHeap.alloc(), ::SHA512_Init, asUpdate(::SHA512_Update), asFinal(::SHA512_Final)) { nativeHeap.free(it.pointed) }
    }


internal fun DigestAlgorithm.linuxDigest(): Pair<Int, CPointer<EVP_MD>> =  when (this) {
    DigestAlgorithm.SHA1 ->   20                   to EVP_sha1()!!
    DigestAlgorithm.SHA224 -> SHA224_DIGEST_LENGTH to EVP_sha224()!!
    DigestAlgorithm.SHA256 -> SHA256_DIGEST_LENGTH to EVP_sha256()!!
    DigestAlgorithm.SHA384 -> SHA384_DIGEST_LENGTH to EVP_sha384()!!
    DigestAlgorithm.SHA512 -> SHA512_DIGEST_LENGTH to EVP_sha512()!!
}

public actual fun DigestWriteable.Companion.newHmacInstance(algorithm: DigestAlgorithm, key: ReadMemory): DigestWriteable {
    val (digestSize, evp) = algorithm.linuxDigest()

    val keyCopy = Allocation.nativeCopy(key)

    val init: NativeDigestInitFunction<HMAC_CTX> = { ctx -> HMAC_Init_ex(ctx, keyCopy.memory.pointer, keyCopy.size.convert(), evp, null) }
    val update: NativeDigestUpdateFunction<HMAC_CTX> = { ctx, ptr, size -> HMAC_Update(ctx, ptr.reinterpret(), size.convert()) }
    val final: NativeDigestFinalFunction<HMAC_CTX> = { ctx, ptr -> HMAC_Final(ctx, ptr.reinterpret(), null) }

    return NativeFunctionsDigestWriteable(digestSize, HMAC_CTX_new()!!.pointed, init, update, final) { ctx ->
        keyCopy.fill(0)
        keyCopy.close()
        HMAC_CTX_free(ctx)
    }
}
