package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.crypto.libssl.ERR_load_CRYPTO_strings
import org.kodein.memory.crypto.libssl.ERR_load_EVP_strings
import org.kodein.memory.crypto.libssl.ERR_print_errors_cb
import org.kodein.memory.io.IOException
import platform.posix.size_t

private fun error_cb(lineStr: CPointer<ByteVar>?, lineLength: size_t, userData: COpaquePointer?): Int {
    val array = ByteArray(lineLength.convert()) { lineStr!![it] }
    val sb = userData!!.asStableRef<StringBuilder>().get()
    sb.appendLine(array.decodeToString())
    return 1
}

internal fun Int.requireOpenSSLSuccess(function: String) {
    if (this == 1) return

    ERR_load_EVP_strings()
    ERR_load_CRYPTO_strings()

    val sb = StableRef.create(StringBuilder())
    val error = try {
        ERR_print_errors_cb(staticCFunction(::error_cb), sb.asCPointer())
        sb.get().toString()
    } finally {
        sb.dispose()
    }

    throw IOException("OpenSSL $function error:\n$error")
}
