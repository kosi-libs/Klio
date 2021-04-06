@file:JsModule("pbkdf2/browser")
@file:JsNonModule
package org.kodein.memory.crypto.lib.pbkdf2

import org.khronos.webgl.Uint8Array


internal external fun pbkdf2Sync(password: Uint8Array, salt: Uint8Array, rounds: Int, keyLength: Int, digestAlgorithm: String): Uint8Array
