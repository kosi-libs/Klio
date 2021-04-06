package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.crypto.cinterop.SHA256_DIGEST_LENGTH
import org.kodein.memory.io.*
//import platform.CoreCrypto.*

public actual fun Digest.Companion.sha1(): Digest =
    DigestNative(SHA256_DIGEST_LENGTH, ::CC_SHA1_Init, ::CC_SHA1_Update, ::CC_SHA1_Final) { nativeHeap.alloc() }
public actual fun Digest.Companion.sha224(): Digest =
    DigestNative(CC_SHA224_DIGEST_LENGTH, ::CC_SHA224_Init, ::CC_SHA224_Update, ::CC_SHA224_Final) { nativeHeap.alloc() }
public actual fun Digest.Companion.sha256(): Digest =
    DigestNative(CC_SHA256_DIGEST_LENGTH, ::CC_SHA256_Init, ::CC_SHA256_Update, ::CC_SHA256_Final) { nativeHeap.alloc() }
public actual fun Digest.Companion.sha384(): Digest =
    DigestNative(CC_SHA384_DIGEST_LENGTH, ::CC_SHA384_Init, ::CC_SHA384_Update, ::CC_SHA384_Final) { nativeHeap.alloc() }
public actual fun Digest.Companion.sha512(): Digest =
    DigestNative(CC_SHA512_DIGEST_LENGTH, ::CC_SHA512_Init, ::CC_SHA512_Update, ::CC_SHA512_Final) { nativeHeap.alloc() }
