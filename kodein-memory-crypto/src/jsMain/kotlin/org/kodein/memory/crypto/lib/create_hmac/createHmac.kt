package org.kodein.memory.crypto.lib.create_hmac

import org.khronos.webgl.Uint8Array
import org.kodein.memory.crypto.lib.JsDigest
import org.kodein.memory.crypto.lib.safe_buffer.Buffer

@JsModule("create-hmac")
@JsNonModule
internal external fun createHmac(algo: String, key: Buffer): JsDigest
