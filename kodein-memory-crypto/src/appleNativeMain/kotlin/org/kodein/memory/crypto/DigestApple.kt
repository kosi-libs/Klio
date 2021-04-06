package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import platform.CoreCrypto.*


private inline fun <C : CVariable> asUpdate(noinline update: (CValuesRef<C>?, CValuesRef<*>?, CC_LONG) -> Unit): DigestUpdate<C> =
    ({ ctx, ptr, size ->
        update(ctx, ptr, size.convert())
    })

private inline fun <C : CVariable> asFinal(noinline final: (CValuesRef<UByteVar>?, CValuesRef<C>?) -> Unit): DigestFinal<C> =
    ({ ctx, output ->
        final(output.reinterpret(), ctx)
    })

public actual fun DigestWriteable.Companion.newInstance(algorithm: DigestAlgorithm): DigestWriteable =
    when (algorithm) {
        DigestAlgorithm.SHA1   -> NativeDigestWriteable(CC_SHA1_DIGEST_LENGTH,   nativeHeap.alloc(), ::CC_SHA1_Init,   asUpdate(::CC_SHA1_Update),   asFinal(::CC_SHA1_Final))
        DigestAlgorithm.SHA224 -> NativeDigestWriteable(CC_SHA224_DIGEST_LENGTH, nativeHeap.alloc(), ::CC_SHA224_Init, asUpdate(::CC_SHA224_Update), asFinal(::CC_SHA224_Final))
        DigestAlgorithm.SHA256 -> NativeDigestWriteable(CC_SHA256_DIGEST_LENGTH, nativeHeap.alloc(), ::CC_SHA256_Init, asUpdate(::CC_SHA256_Update), asFinal(::CC_SHA256_Final))
        DigestAlgorithm.SHA384 -> NativeDigestWriteable(CC_SHA384_DIGEST_LENGTH, nativeHeap.alloc(), ::CC_SHA384_Init, asUpdate(::CC_SHA384_Update), asFinal(::CC_SHA384_Final))
        DigestAlgorithm.SHA512 -> NativeDigestWriteable(CC_SHA512_DIGEST_LENGTH, nativeHeap.alloc(), ::CC_SHA512_Init, asUpdate(::CC_SHA512_Update), asFinal(::CC_SHA512_Final))
    }


public actual fun DigestWriteable.Companion.newHmacInstance(algorithm: DigestAlgorithm, key: ReadMemory): DigestWriteable {
    val (digestSize, kAlgorithm) = when (algorithm) {
        DigestAlgorithm.SHA1 -> CC_SHA1_DIGEST_LENGTH to kCCHmacAlgSHA1
        DigestAlgorithm.SHA224 -> CC_SHA224_DIGEST_LENGTH to kCCHmacAlgSHA224
        DigestAlgorithm.SHA256 -> CC_SHA256_DIGEST_LENGTH to kCCHmacAlgSHA256
        DigestAlgorithm.SHA384 -> CC_SHA384_DIGEST_LENGTH to kCCHmacAlgSHA384
        DigestAlgorithm.SHA512 -> CC_SHA512_DIGEST_LENGTH to kCCHmacAlgSHA512
    }

    val keyCopy = Allocation.nativeCopy(key)

    val update: DigestUpdate<CCHmacContext> = { ctx, ptr, size -> CCHmacUpdate(ctx, ptr, size.convert()) }
    val init: DigestInit<CCHmacContext> = {
        CCHmacInit(it, kAlgorithm, keyCopy.memory.pointer, keyCopy.size.convert())
    }

    val ctx = nativeHeap.alloc<CCHmacContext>()
    return NativeDigestWriteable(digestSize, ctx, init, update, ::CCHmacFinal, onClose = { keyCopy.fill(0) ; keyCopy.close() })
}
