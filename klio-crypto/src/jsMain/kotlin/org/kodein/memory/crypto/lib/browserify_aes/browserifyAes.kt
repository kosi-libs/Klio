@file:JsModule("browserify-aes/browser")
@file:JsNonModule
package org.kodein.memory.crypto.lib.browserify_aes

import org.khronos.webgl.Uint8Array
import org.kodein.memory.crypto.lib.safe_buffer.Buffer


internal external interface JsCipher {
    fun update(data: Buffer): Uint8Array
    fun final(): Uint8Array
}

internal external fun createCipheriv(algorithm: String, key: Uint8Array, iv: Uint8Array?): JsCipher
internal external fun createDecipheriv(algorithm: String, key: Uint8Array, iv: Uint8Array?): JsCipher
