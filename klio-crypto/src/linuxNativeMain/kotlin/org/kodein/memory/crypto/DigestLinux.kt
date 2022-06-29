package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import org.kodein.memory.crypto.libssl.*
import platform.posix.size_t


private inline fun <C : CVariable> asInit(noinline init: (CValuesRef<C>?) -> Int, name: String): NativeDigestInitFunction<C> =
    ({ ctx ->
        init(ctx).requireOpenSSLSuccess(name)
    })

private inline fun <C : CVariable> asUpdate(noinline update: (CValuesRef<C>?, CValuesRef<*>?, size_t) -> Int, name: String): NativeDigestUpdateFunction<C> =
    ({ ctx, ptr, size ->
        update(ctx, ptr, size.convert()).requireOpenSSLSuccess(name)
    })

private inline fun <C : CVariable> asFinal(noinline final: (CValuesRef<UByteVar>?, CValuesRef<C>?) -> Int, name: String): NativeDigestFinalFunction<C> =
    ({ ctx, output ->
        final(output.reinterpret(), ctx).requireOpenSSLSuccess(name)
    })

public actual fun DigestWriteable.Companion.newInstance(algorithm: DigestAlgorithm): DigestWriteable =
    when (algorithm) {
        DigestAlgorithm.SHA1 -> NativeFunctionsDigestWriteable(
            20,
            nativeHeap.alloc(),
            asInit(::SHA1_Init,   "SHA1_Init"),
            asUpdate(::SHA1_Update, "SHA1_Update"),
            asFinal(::SHA1_Final, "SHA1_Final")
        ) { nativeHeap.free(it.pointed) }

        DigestAlgorithm.SHA256 -> NativeFunctionsDigestWriteable(
            SHA256_DIGEST_LENGTH,
            nativeHeap.alloc(),
            asInit(::SHA256_Init, "SHA256_Init"),
            asUpdate(::SHA256_Update, "SHA256_Update"),
            asFinal(::SHA256_Final, "SHA256_Final")
        ) { nativeHeap.free(it.pointed) }

        DigestAlgorithm.SHA384 -> NativeFunctionsDigestWriteable(
            SHA384_DIGEST_LENGTH,
            nativeHeap.alloc(),
            asInit(::SHA384_Init, "SHA384_Init"),
            asUpdate(::SHA384_Update, "SHA384_Update"),
            asFinal(::SHA384_Final, "SHA384_Final")
        ) { nativeHeap.free(it.pointed) }

        DigestAlgorithm.SHA512 -> NativeFunctionsDigestWriteable(
            SHA512_DIGEST_LENGTH,
            nativeHeap.alloc(),
            asInit(::SHA512_Init, "SHA512_Init"),
            asUpdate(::SHA512_Update, "SHA512_Update"),
            asFinal(::SHA512_Final, "SHA512_Final")
        ) { nativeHeap.free(it.pointed) }
    }

internal fun DigestAlgorithm.linuxDigest(): Pair<Int, CPointer<EVP_MD>> =  when (this) {
    DigestAlgorithm.SHA1 ->   20                   to EVP_sha1()!!
    DigestAlgorithm.SHA256 -> SHA256_DIGEST_LENGTH to EVP_sha256()!!
    DigestAlgorithm.SHA384 -> SHA384_DIGEST_LENGTH to EVP_sha384()!!
    DigestAlgorithm.SHA512 -> SHA512_DIGEST_LENGTH to EVP_sha512()!!
}

public actual fun DigestWriteable.Companion.newHmacInstance(algorithm: DigestAlgorithm, key: ReadMemory): DigestWriteable {
    val (digestSize, evp) = algorithm.linuxDigest()

    val keyCopy = Allocation.nativeCopy(key)

    val init: NativeDigestInitFunction<HMAC_CTX> = { ctx -> HMAC_Init_ex(ctx, keyCopy.memory.pointer, keyCopy.size.convert(), evp, null).requireOpenSSLSuccess("HMAC_Init_ex") }
    val update: NativeDigestUpdateFunction<HMAC_CTX> = { ctx, ptr, size -> HMAC_Update(ctx, ptr.reinterpret(), size.convert()).requireOpenSSLSuccess("HMAC_Update") }
    val final: NativeDigestFinalFunction<HMAC_CTX> = { ctx, ptr -> HMAC_Final(ctx, ptr.reinterpret(), null).requireOpenSSLSuccess("HMAC_Final") }

    return NativeFunctionsDigestWriteable(digestSize, HMAC_CTX_new()!!.pointed, init, update, final) { ctx ->
        keyCopy.fill(0)
        keyCopy.close()
        HMAC_CTX_free(ctx)
    }
}
