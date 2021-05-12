package org.kodein.memory.crypto

import java.util.*
import javax.crypto.spec.SecretKeySpec


internal fun SecretKeySpec.safeDestroy() {
    try {
        destroy()
    } catch (_: Throwable) {
        try {
            val keyField = javaClass.getDeclaredField("key")
            keyField.isAccessible = true
            val key = keyField.get(this) as ByteArray
            Arrays.fill(key, 0)
            keyField.set(this, null)
        } catch (_: Throwable) {}
    }
}
